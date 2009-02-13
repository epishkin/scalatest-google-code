/*
 * Copyright 2001-2008 Artima, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.scalatest.tools

import java.awt.BorderLayout
import java.awt.Container
import java.awt.Dimension
import java.awt.GridLayout
import java.awt.Point
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.KeyEvent
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintWriter
import java.net.URL
import javax.swing.AbstractAction
import javax.swing.DefaultListModel
import javax.swing.ImageIcon
import javax.swing.JButton
import javax.swing.JCheckBoxMenuItem
import javax.swing.JDialog
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.JMenu
import javax.swing.JMenuBar
import javax.swing.JMenuItem
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextArea
import javax.swing.KeyStroke
import javax.swing.ListSelectionModel
import javax.swing.border.BevelBorder
import javax.swing.border.EmptyBorder
import javax.swing.event.ListSelectionEvent
import javax.swing.event.ListSelectionListener
import Runner.usingEventDispatchThread
import Runner.withClassLoaderAndDispatchReporter
import java.util.concurrent.Semaphore
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.awt.EventQueue

/**
 * The main class for Runner's GUI.
 *
 * reportTypesToCollect are the types of reports that should be collected as a run runs.
 * This comes from the set of config options following the -g in the invocation of Runner.
 * If it is -gZ, for example, only test starting reports will be collected as the runs run.
 * We don't collect options that aren't selected, because long runs can generate a lot of
 * reports that would take up a lot of memory.
 *
 * @author Bill Venners
 */
private[scalatest] class RunnerJFrame(recipeName: Option[String], val reportTypesToCollect: ReporterOpts.Set32,
    reporterSpecs: ReporterSpecs, suitesList: List[String], runpathList: List[String], includes: Set[String],
    excludes: Set[String], propertiesMap: Map[String, String], concurrent: Boolean, memberOfList: List[String], beginsWithList: List[String],
    testNGList: List[String]) extends
    JFrame(RunnerJFrame.getTitle(recipeName)) with RunDoneListener with RunnerGUI {

  // This should only be updated by the event handler thread.
  private var currentState: RunnerGUIState = RunningState

  // The default options in the graphic view. Just show runs
  // and failures. This is also a selection in the View menu.
  private val runsAndFailures =
    new ReporterOpts.Set32(
      ReporterOpts.PresentRunStarting.mask32
      | ReporterOpts.PresentTestFailed.mask32
      | ReporterOpts.PresentSuiteAborted.mask32
      | ReporterOpts.PresentRunStopped.mask32
      | ReporterOpts.PresentRunAborted.mask32
      | ReporterOpts.PresentRunCompleted.mask32
    )

  // These are the actual options to view in the list of reports.
  // This must be the same set or a subset of reportTypesToCollect,
  // because you can't view something that wasn't collected.
  // This should only be updated by the event handler thread.
  private var viewOptions = runsAndFailures

  private val optionsMap: Map[ReporterOpts.Value, JCheckBoxMenuItem] = initializeOptionsMap

  private val aboutBox: AboutJDialog = initializeAboutBox()

  // The list of reports collected from the most recent run
  // The most recently added report is at the head of the list.
  private var collectedReports: List[ReportHolder] = Nil

  // The reportsListModel and reportsJList are used to display the current
  // collected reports of types selected by the view menu
  private val reportsListModel: DefaultListModel = new DefaultListModel()
  private val reportsJList: JList = new JList(reportsListModel)

  // The detailsJTextArea displays the text details of a report.
  private val detailsJTextArea: JTextArea = new JTextArea()

  private val progressBar: ColorBar = new ColorBar()
  private val statusJPanel: StatusJPanel = new StatusJPanel()
  private val rerunColorBox: ColorBar = new ColorBar()
  private val runJButton: JButton = new JButton(Resources("Run"))
  private val rerunJButton: JButton = new JButton(Resources("Rerun"))

  private var testsCompletedCount: Int = 0
  private var rerunTestsCompletedCount: Int = 0

  private val graphicRunReporter: Reporter = new GraphicRunReporter
  private val graphicRerunReporter: Reporter = new GraphicRerunReporter

  private val stopper = new SimpleStopper

  private val exitSemaphore = new Semaphore(1)

  initialize()

  private def initialize() = {

    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)

    val ambientURL: URL = classOf[Suite].getClassLoader().getResource("images/greendot.gif")
    val ambientIcon: ImageIcon = new ImageIcon(ambientURL)
    setIconImage(ambientIcon.getImage())

    setupMenus()

    runJButton.setMnemonic(KeyEvent.VK_R)
    runJButton.addActionListener(
      new ActionListener() {
        def actionPerformed(ae: ActionEvent) {
          currentState = currentState.runButtonPressed(RunnerJFrame.this)
        }
      }
    )

    val progressBarHolder: JPanel = new JPanel()
    progressBarHolder.setLayout(new BorderLayout())
    progressBarHolder.setBorder(new BevelBorder(BevelBorder.LOWERED))
    progressBarHolder.add(progressBar, BorderLayout.CENTER)
    val pBarRunBtnJPanel: JPanel = new JPanel()

    pBarRunBtnJPanel.setLayout(new BorderLayout(5, 5))
    pBarRunBtnJPanel.add(progressBarHolder, BorderLayout.CENTER)
    pBarRunBtnJPanel.add(runJButton, BorderLayout.EAST)

    val progressJPanel: JPanel = new JPanel()

    progressJPanel.setLayout(new GridLayout(2, 1))
    progressJPanel.add(statusJPanel)
    progressJPanel.add(pBarRunBtnJPanel)
    val reportsJLabel: JLabel = new JLabel(Resources("reportsLabel"))

    val southHuggingReportsLabelJPanel: JPanel = new JPanel()

    southHuggingReportsLabelJPanel.setLayout(new BorderLayout())
    southHuggingReportsLabelJPanel.add(reportsJLabel, BorderLayout.SOUTH)
    southHuggingReportsLabelJPanel.setBorder(new EmptyBorder(0, 1, 0, 0))

    reportsJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
    reportsJList.setCellRenderer(new IconEmbellishedListCellRenderer())
    val reportsJScrollPane: JScrollPane = new JScrollPane(reportsJList)

    val reportsJPanel: JPanel = new JPanel()

    reportsJPanel.setLayout(new BorderLayout())
    reportsJPanel.add(southHuggingReportsLabelJPanel, BorderLayout.NORTH)
    reportsJPanel.add(reportsJScrollPane, BorderLayout.CENTER)
    val detailsJLabel: JLabel = new JLabel(Resources("detailsLabel"))

    val southHuggingDetailsLabelJPanel: JPanel = new JPanel()

    southHuggingDetailsLabelJPanel.setLayout(new BorderLayout())
    southHuggingDetailsLabelJPanel.add(detailsJLabel, BorderLayout.SOUTH)
    southHuggingDetailsLabelJPanel.setBorder(new EmptyBorder(0, 1, 0, 0))

    rerunJButton.setMnemonic(KeyEvent.VK_E)
    rerunJButton.setEnabled(false)

    val rerunColorBoxHolder: JPanel = new JPanel()

    rerunColorBoxHolder.setLayout(new BorderLayout())
    rerunColorBoxHolder.setBorder(new BevelBorder(BevelBorder.LOWERED))
    rerunColorBoxHolder.add(rerunColorBox, BorderLayout.CENTER)
    val rerunJPanel: JPanel = new JPanel()

    rerunJPanel.setLayout(new GridLayout(1, 2, 5, 5))
    rerunJPanel.add(rerunColorBoxHolder)
    rerunJPanel.add(rerunJButton)
    rerunJPanel.setBorder(new EmptyBorder(0, 0, 5, 0))
    val detailsNorthJPanel: JPanel = new JPanel()

    detailsNorthJPanel.setLayout(new BorderLayout())
    detailsNorthJPanel.add(BorderLayout.WEST, southHuggingDetailsLabelJPanel)
    detailsNorthJPanel.add(BorderLayout.EAST, rerunJPanel)

    detailsJTextArea.setEditable(false)
    detailsJTextArea.setLineWrap(true)
    detailsJTextArea.setWrapStyleWord(true)
    val detailsJScrollPane: JScrollPane = new JScrollPane(detailsJTextArea)

    val detailsJPanel: JPanel = new JPanel()

    detailsJPanel.setLayout(new BorderLayout())
    detailsJPanel.add(detailsNorthJPanel, BorderLayout.NORTH)
    detailsJPanel.add(detailsJScrollPane, BorderLayout.CENTER)
    val reportsDetailsPanel: JPanel = new JPanel()

    reportsDetailsPanel.setLayout(new GridLayout(2, 1, 5, 5))
    reportsDetailsPanel.add(reportsJPanel)
    reportsDetailsPanel.add(detailsJPanel)
    val reporterJPanel: JPanel = new JPanel()

    reporterJPanel.setLayout(new BorderLayout(5, 5))
    reporterJPanel.add(progressJPanel, BorderLayout.NORTH)
    reporterJPanel.add(reportsDetailsPanel, BorderLayout.CENTER)
    reportsJList.addListSelectionListener(
      new ListSelectionListener() {
        def valueChanged(e: ListSelectionEvent) {

          val rh: ReportHolder = reportsJList.getSelectedValue().asInstanceOf[ReportHolder]

          if (rh == null) {

            // This means nothing is currently selected
            detailsJTextArea.setText("")
            currentState = currentState.listSelectionChanged(RunnerJFrame.this)
          }
          else {

            val report: Report = rh.report
            val reportType: ReporterOpts.Value = rh.reportType
            val isRerun: Boolean = rh.isRerun
  
            if (isRerun)
              detailsJTextArea.setText(Resources("RERUN_" + ReporterOpts.getUpperCaseName(reportType)))
            else
              detailsJTextArea.setText(Resources(ReporterOpts.getUpperCaseName(reportType)))

            // The only return value from a report that can be null is getThrowable's.

            detailsJTextArea.append("\n" + Resources("DetailsName") + ": " + report.name)
            detailsJTextArea.append("\n" + Resources("DetailsMessage") + ": " + report.message)
            detailsJTextArea.append("\n" + Resources("DetailsDate") + ": " + report.date)
            detailsJTextArea.append("\n" + Resources("DetailsThread") + ": " + report.threadName)
  
            detailsJTextArea.append("\n" + Resources("DetailsThrowable"))

            report.throwable match {
              case Some(t) => {
                val bytestream: ByteArrayOutputStream = new ByteArrayOutputStream()
                val printwriter: PrintWriter = new PrintWriter(bytestream)
                t.printStackTrace(printwriter)
                printwriter.close()
                detailsJTextArea.append(":\n" + bytestream.toString())
              }
              case None => detailsJTextArea.append(": None")
            }
  
            detailsJTextArea.setCaretPosition(0)
            currentState = currentState.listSelectionChanged(RunnerJFrame.this)
          }
        }
      }
    )

    rerunJButton.addActionListener(
      new ActionListener() {
        def actionPerformed(ae: ActionEvent) {
          currentState = currentState.rerunButtonPressed(RunnerJFrame.this)
        }
      }
    )
    val mainJPanel: JPanel = new JPanel()

    mainJPanel.setLayout(new BorderLayout(5, 5))
    mainJPanel.setBorder(new EmptyBorder(5, 5, 5, 5))
    mainJPanel.add(reporterJPanel, BorderLayout.CENTER)
    val pane: Container = getContentPane()

    pane.setLayout(new GridLayout(1, 1))
    pane.add(mainJPanel)

    // Set the size of both buttons to the max of the localized labels Run, Rerun, and Stop
    val runButtonSize: Dimension = runJButton.getPreferredSize()
    val rerunButtonSize: Dimension = rerunJButton.getPreferredSize()
    // Create a throw away button to get the size of Stop
    val stopButtonSize: Dimension = new JButton(Resources("Stop")).getPreferredSize()

    val preferredSize = new Dimension(
      runButtonSize.width.max(rerunButtonSize.width.max(stopButtonSize.width)),
      runButtonSize.height.max(rerunButtonSize.height.max(stopButtonSize.height))
    )

    runJButton.setPreferredSize(preferredSize)
    rerunJButton.setPreferredSize(preferredSize)

    exitSemaphore.acquire()
    addWindowListener(
      new WindowAdapter {
        override def windowClosed(e: WindowEvent) { exitSemaphore.release() }
      }
    )

    pack()

    val dim: Dimension = getSize()
    dim.height = dim.height / 5 + dim.height
    dim.width = dim.height / 3 * 4
    setSize(dim)
  }

  private[scalatest] def blockUntilWindowClosed() {
    exitSemaphore.acquire()
  }

  // This initialize method idiom is a way to get rid of a var
  // when you have verbose initialization.
  private def initializeAboutBox() = {
    val title2: String = Resources("AboutBoxTitle")
    new AboutJDialog(RunnerJFrame.this, title2)
  }

  private def setupMenus() {

    val menuBar: JMenuBar = new JMenuBar()

    // The ScalaTest menu 
    val scalaTestMenu: JMenu = new JMenu(Resources("ScalaTestMenu"))
    scalaTestMenu.setMnemonic(KeyEvent.VK_S)
    menuBar.add(scalaTestMenu)

    // The ScalaTest.About menu item
    val aboutItem: JMenuItem = new JMenuItem(Resources("About"), KeyEvent.VK_A)
    scalaTestMenu.add(aboutItem)
    aboutItem.addActionListener(
      new ActionListener() {
        def actionPerformed(ae: ActionEvent) {
          val location: Point = getLocation()
          location.x += 20
          location.y += 6
          aboutBox.setLocation(location)
          aboutBox.setVisible(true)
        }
      }
    )

    scalaTestMenu.addSeparator()

    // The ScalaTest.Exit menu item
    val exitItem: JMenuItem = new JMenuItem(Resources("Exit"), KeyEvent.VK_X)
    scalaTestMenu.add(exitItem)
    exitItem.addActionListener(
      new ActionListener() {
        def actionPerformed(ae: ActionEvent) {
          dispose()
          System.exit(0)
        }
      }
    )

    // The View menu
    val viewMenu = new JMenu(Resources("ViewMenu"))
    viewMenu.setMnemonic(KeyEvent.VK_V)

    // the View.Runs and Failures menu item
    val runsFailuresItem: JMenuItem = new JMenuItem(Resources("runsFailures"), KeyEvent.VK_F)
    runsFailuresItem.setAccelerator(KeyStroke.getKeyStroke("control F"))
    viewMenu.add(runsFailuresItem)
    runsFailuresItem.addActionListener(
      new ActionListener() {
        def actionPerformed(ae: ActionEvent) {
          viewOptions = runsAndFailures ** reportTypesToCollect
          updateViewOptionsAndReportsList()
        }
      }
    )

    val allReportsItem: JMenuItem = new JMenuItem(Resources("allReports"), KeyEvent.VK_A)
    allReportsItem.setAccelerator(KeyStroke.getKeyStroke("control L"))
    viewMenu.add(allReportsItem)
    allReportsItem.addActionListener(
      new ActionListener() {
        def actionPerformed(ae: ActionEvent) {
          viewOptions = reportTypesToCollect
          updateViewOptionsAndReportsList()
        }
      }
    )

    viewMenu.addSeparator()

    // Add the checkboxes in the correct order
    viewMenu.add(optionsMap(ReporterOpts.PresentRunStarting))
    viewMenu.add(optionsMap(ReporterOpts.PresentTestStarting))
    viewMenu.add(optionsMap(ReporterOpts.PresentTestSucceeded))
    viewMenu.add(optionsMap(ReporterOpts.PresentTestFailed))
    viewMenu.add(optionsMap(ReporterOpts.PresentTestIgnored))
    viewMenu.add(optionsMap(ReporterOpts.PresentSuiteStarting))
    viewMenu.add(optionsMap(ReporterOpts.PresentSuiteCompleted))
    viewMenu.add(optionsMap(ReporterOpts.PresentSuiteAborted))
    viewMenu.add(optionsMap(ReporterOpts.PresentInfoProvided))
    viewMenu.add(optionsMap(ReporterOpts.PresentRunStopped))
    viewMenu.add(optionsMap(ReporterOpts.PresentRunCompleted))
    viewMenu.add(optionsMap(ReporterOpts.PresentRunAborted))

    menuBar.add(viewMenu)
    setJMenuBar(menuBar)
  }

  private def initializeOptionsMap(): Map[ReporterOpts.Value, JCheckBoxMenuItem] = {

    // TODO: Why am I using an immutable map here. Better a val with a mutable map I'd think.
    var map: Map[ReporterOpts.Value, JCheckBoxMenuItem] = Map()

    for (option <- ReporterOpts.allOptions) {

      val rawOptionName = ReporterOpts.getUpperCaseName(option)
      val menuItemText: String = Resources("MENU_" + rawOptionName)

      val itemAction: AbstractAction =
        new AbstractAction(menuItemText) {
          def actionPerformed(ae: ActionEvent) {

            val checkBox: JCheckBoxMenuItem = ae.getSource().asInstanceOf[JCheckBoxMenuItem]
            val option = getValue("option").asInstanceOf[ReporterOpts.Value]

            if (viewOptions.contains(option))
              viewOptions = viewOptions - option
            else
              viewOptions = viewOptions + option

            val checked: Boolean = viewOptions.contains(option)
            checkBox.setState(checked)

            // Now, since the configuration changed, we need to update the
            // list display appropriately:
            refreshReportsJList()
          }
        }

      val checked: Boolean = viewOptions.contains(option)
      val checkBox: JCheckBoxMenuItem = new JCheckBoxMenuItem(itemAction)

      checkBox.setState(checked)

      // Put the option into the checkbox's AbstractAction, so it can be
      // taken out when the checkbox is checked or unchecked.
      itemAction.putValue("option", option)

      map = map + (option -> checkBox)
    }

    map
  }

  def requestStop() {
    stopper.requestStop()
  }

  private def updateViewOptionsAndReportsList() {

    for (option <- ReporterOpts.allOptions) {

      val box: JCheckBoxMenuItem = optionsMap(option)

      if (reportTypesToCollect.contains(option)) {
        box.setEnabled(true)
        if (viewOptions.contains(option))
          box.setSelected(true)
        else
          box.setSelected(false)
      }
      else {
        box.setSelected(false)
        box.setEnabled(false)
      }
    }

    // Now, since the configuration changed, we need to update the
    // list display appropriately:
    refreshReportsJList()
  }

  private def refreshReportsJList() {

    val formerlySelectedItem: ReportHolder = reportsJList.getSelectedValue().asInstanceOf[ReportHolder]

    // clear the list of reports and the detail area
    reportsListModel.clear()
    detailsJTextArea.setText("")

    for (rh <- collectedReports.reverse; if viewOptions.contains(rh.reportType)) {
      val shouldAddElement = rh.report match {
        case sr: SpecReport => sr.includeInSpecOutput
        case _ => true
      }
      if (shouldAddElement) reportsListModel.addElement(rh)
    }

    // Isn't there a risk that the formerly selected item will no longer exist in the list?
    // Does this result in an exception? Of course the stupid JavaDoc API docs is silent on this.
    // TODO: try this and fix if need be
    reportsJList.setSelectedValue(formerlySelectedItem, true)
  }

  private def registerReport(report: Report, reportType: ReporterOpts.Value) {
    registerRunOrRerunReport(report, reportType, false)
  }

  private def registerRerunReport(report: Report, reportType: ReporterOpts.Value) {
    registerRunOrRerunReport(report, reportType, true)
  }

  private def registerRunOrRerunReport(report: Report, reportType: ReporterOpts.Value, isRerun: Boolean) {

    val reportHolder: ReportHolder = new ReportHolder(report, reportType, isRerun)

    if (reportTypesToCollect.contains(reportType)) {
      collectedReports = reportHolder :: collectedReports
      if (viewOptions.contains(reportType))
        reportsListModel.addElement(reportHolder)
    }
  }

  private class GraphicRunReporter extends Reporter {

    override def testFailed(report: Report) {
      if (report == null)
        throw new NullPointerException("report is null")
  
      usingEventDispatchThread {
        testsCompletedCount += 1
        // Passing in false here increments the test failed count
        // in the statusJPanel, which updates the counter on the GUI
        statusJPanel.setTestsRun(testsCompletedCount, false)
        progressBar.setValue(testsCompletedCount)
        progressBar.setRed()
        registerReport(report, ReporterOpts.PresentTestFailed)
      }
    }
  
    override def testSucceeded(report: Report) {
      if (report == null)
        throw new NullPointerException("report is null")
  
      usingEventDispatchThread {
        testsCompletedCount += 1
        statusJPanel.setTestsRun(testsCompletedCount, true)
        progressBar.setValue(testsCompletedCount)
        registerReport(report, ReporterOpts.PresentTestSucceeded)
      }
    }
  
    override def testIgnored(report: Report) {
      if (report == null)
        throw new NullPointerException("report is null")
  
      usingEventDispatchThread {
        registerReport(report, ReporterOpts.PresentTestIgnored)
      }
    }
  
    override def testStarting(report: Report) {
      if (report == null)
        throw new NullPointerException("report is null")
  
      usingEventDispatchThread {
        registerReport(report, ReporterOpts.PresentTestStarting)
      }
    }
  
    override def infoProvided(report: Report) {
      if (report == null)
        throw new NullPointerException("report is null")
  
      usingEventDispatchThread {
        registerReport(report, ReporterOpts.PresentInfoProvided)
      }
    }
  
    override def runStarting(testCount: Int) {
  
      if (testCount < 0)
        throw new IllegalArgumentException()
  
      // Create the Report outside of the event handler thread, because otherwise
      // the event handler thread shows up as the originating thread of this report,
      // and that looks bad and is wrong to boot.
      val stringToReport: String = Resources("runStarting", testCount.toString)
      val report: Report = new Report("org.scalatest.tools.Runner", stringToReport)
      val reportHolder: ReportHolder = new ReportHolder(report, ReporterOpts.PresentRunStarting)

      usingEventDispatchThread {
        testsCompletedCount = 0
        progressBar.setMax(testCount)
        progressBar.setValue(0)
        progressBar.setGreen()
  
        statusJPanel.reset()
        statusJPanel.setTestsExpected(testCount)
  
        // This should already have been cleared by prepUIForStarting, but
        // doing it again here for the heck of it.
        collectedReports = reportHolder :: Nil
        reportsListModel.clear()
  
        detailsJTextArea.setText("")
  
        if (viewOptions.contains(ReporterOpts.PresentRunStarting))
          reportsListModel.addElement(reportHolder)
      }
    }
  
    override def runCompleted() {
      // Create the Report outside of the event handler thread, because otherwise
      // the event handler thread shows up as the originating thread of this report,
      // and that looks bad and is wrong to boot.
      val stringToReport: String = Resources("runCompleted", testsCompletedCount.toString)
      val report: Report = new Report("", stringToReport)
      usingEventDispatchThread {
        registerReport(report, ReporterOpts.PresentRunCompleted)
      }
    }
  
    override def suiteStarting(report: Report) {
      if (report == null)
        throw new NullPointerException("report is null")
      usingEventDispatchThread {
        registerReport(report, ReporterOpts.PresentSuiteStarting)
      }
    }
  
    override def suiteCompleted(report: Report) {
      if (report == null)
        throw new NullPointerException("report is null")
      usingEventDispatchThread {
        registerReport(report, ReporterOpts.PresentSuiteCompleted)
      }
    }
  
    override def suiteAborted(report: Report) {
      if (report == null)
        throw new NullPointerException("report is null")
      usingEventDispatchThread {
        progressBar.setRed()
        registerReport(report, ReporterOpts.PresentSuiteAborted)
      }
    }
  
    override def runStopped() {
      // Create the Report outside of the event handler thread, because otherwise
      // the event handler thread shows up as the originating thread of this report,
      // and that looks bad and is actually wrong.
      val stringToReport: String = Resources("runStopped", testsCompletedCount.toString)
      val report: Report = new Report("org.scalatest.tools.Runner", stringToReport)
      usingEventDispatchThread {
        registerReport(report, ReporterOpts.PresentRunStopped)
      }
    }
  
    override def runAborted(report: Report) {
      if (report == null)
        throw new NullPointerException("report is null")
      usingEventDispatchThread {
        progressBar.setRed()
        registerReport(report, ReporterOpts.PresentRunAborted)
      }
    }
  }

  // Invoked when a test is done. This is used to turn the Run button back on after
  // a Stop request has disabled it. When this method is invoked by the runner, it
  // means that the run has finished, so that it is OK to enable Run again.
  override def done() {
    usingEventDispatchThread {
      currentState = currentState.runFinished(RunnerJFrame.this)
    }
  }

  // Called from the main thread initially, thereafter from the event handler thread
  override def runFromGUI() {
    (new RunnerThread).start()
  }

  // Must be called from event handler thread
  override def rerunFromGUI(rerunnable: Rerunnable) {
    assert(EventQueue.isDispatchThread)
    (new RerunnerThread(rerunnable)).start()
  }

  // This must be called by the event handler thread
  def prepUIForRunning() {
    assert(EventQueue.isDispatchThread)
    val stopText: String = Resources("Stop")
    val rerunText: String = Resources("Rerun")
    runJButton.setText(stopText)
    rerunJButton.setText(rerunText)
    runJButton.setEnabled(true)
    rerunJButton.setEnabled(false)
    rerunColorBox.setGray()
    progressBar.setGray()
    statusJPanel.reset()
    statusJPanel.setTestsExpected(0)
    collectedReports = Nil
    reportsListModel.clear()
    detailsJTextArea.setText("")
  }

  // This must be called by the event handler thread
  def prepUIWhileRunning() {
    assert(EventQueue.isDispatchThread)
    val stopText: String = Resources("Stop")
    val rerunText: String = Resources("Rerun")
    runJButton.setText(stopText)
    rerunJButton.setText(rerunText)
    runJButton.setEnabled(true)
    rerunJButton.setEnabled(false)
    rerunColorBox.setGray()
  }

  // This must be called by the event handler thread
  def prepUIForRerunning() {
    assert(EventQueue.isDispatchThread)
    val runText: String = Resources("Run")
    val stopText: String = Resources("Stop")
    runJButton.setText(runText)
    rerunJButton.setText(stopText)
    runJButton.setEnabled(false)
    rerunJButton.setEnabled(true)
    rerunColorBox.setGray()
  }

  // This must be called by the event handler thread
  def prepUIWhileRerunning() {
    assert(EventQueue.isDispatchThread)
    val runText: String = Resources("Run")
    val stopText: String = Resources("Stop")
    runJButton.setText(runText)
    rerunJButton.setText(stopText)
    runJButton.setEnabled(false)
    rerunJButton.setEnabled(true)
  }

  // This must be called by the event handler thread
  def prepUIForReady() {
    assert(EventQueue.isDispatchThread)
    val runText: String = Resources("Run")
    val rerunText: String = Resources("Rerun")
    runJButton.setText(runText)
    rerunJButton.setText(rerunText)
    runJButton.setEnabled(true)
    val rh: ReportHolder = reportsJList.getSelectedValue().asInstanceOf[ReportHolder] 
    rerunJButton.setEnabled(rh != null && rh.report.rerunnable.isDefined)
  }

  // This must be called by the event handler thread
  def prepUIForStopping() {
    assert(EventQueue.isDispatchThread)
    val stopText: String = Resources("Stop")
    val rerunText: String = Resources("Rerun")
    runJButton.setText(stopText)
    rerunJButton.setText(rerunText)
    runJButton.setEnabled(false)
    rerunJButton.setEnabled(false)
  }

  // This must be called by the event handler thread
  def prepUIForReStopping() {
    assert(EventQueue.isDispatchThread)
    val runText: String = Resources("Run")
    val stopText: String = Resources("Stop")
    runJButton.setText(runText)
    rerunJButton.setText(stopText)
    runJButton.setEnabled(false)
    rerunJButton.setEnabled(false)
  }

  // This must be called by the event handler thread
  def selectFirstFailureIfExists() {
    assert(EventQueue.isDispatchThread)
    val rh: ReportHolder = reportsJList.getSelectedValue.asInstanceOf[ReportHolder] 
    if (rh == null) { // Only do this if something isn't already selected
      val model = reportsJList.getModel
      // the imperative methods on model push strongly to an imperative style
      var indexToSelect = -1
      var i = 0
      var done = false
      while (i < model.getSize && !done) {
        val rh = model.getElementAt(i).asInstanceOf[ReportHolder]
        val reportType = rh.reportType
        if (reportType == ReporterOpts.PresentTestFailed || reportType == ReporterOpts.PresentRunAborted || 
            reportType == ReporterOpts.PresentSuiteAborted) {

          reportsJList.setSelectedValue(rh, true)
          done = true
        }
        i += 1
      }
    }
  }

  def getSelectedRerunnable(): Option[Rerunnable] = {
    val rh: ReportHolder = reportsJList.getSelectedValue().asInstanceOf[ReportHolder] 
    if (rh == null)
      None
    else
      rh.report.rerunnable
  }

  private class GraphicRerunReporter extends Reporter {

    override def runStarting(testCount: Int) {
      if (testCount < 0)
        throw new IllegalArgumentException()
  
      // Create the Report outside of the event handler thread, because otherwise
      // the event handler thread shows up as the originating thread of this report,
      // and that looks bad and is actually wrong.
      val stringToReport: String = Resources("rerunStarting", testCount.toString)
      val report: Report = new Report("org.scalatest.tools.Runner", stringToReport)

      usingEventDispatchThread {
        rerunTestsCompletedCount = 0
        rerunColorBox.setMax(testCount)
        rerunColorBox.setValue(0)
        rerunColorBox.setGreen()
  
        registerRerunReport(report, ReporterOpts.PresentRunStarting)
      }
    }
  
    override def testStarting(report: Report) {
      if (report == null)
        throw new NullPointerException("report is null")
      usingEventDispatchThread {
        registerRerunReport(report, ReporterOpts.PresentTestStarting)
      }
    }
  
    override def testSucceeded(report: Report) {
      if (report == null)
        throw new NullPointerException("report is null")
      usingEventDispatchThread {
        rerunTestsCompletedCount += 1
        rerunColorBox.setValue(rerunTestsCompletedCount)
        registerRerunReport(report, ReporterOpts.PresentTestSucceeded)
      }
    }
  
    override def testIgnored(report: Report) {
      if (report == null)
        throw new NullPointerException("report is null")
      usingEventDispatchThread {
        rerunColorBox.setValue(rerunTestsCompletedCount)
        registerRerunReport(report, ReporterOpts.PresentTestIgnored)
      }
    }
  
    override def testFailed(report: Report) {
      if (report == null)
        throw new NullPointerException("report is null")
      usingEventDispatchThread {
        rerunTestsCompletedCount += 1
        rerunColorBox.setValue(rerunTestsCompletedCount)
        rerunColorBox.setRed()
        registerRerunReport(report, ReporterOpts.PresentTestFailed)
      }
    }
  
    override def infoProvided(report: Report) {
      if (report == null)
        throw new NullPointerException("report is null")
      usingEventDispatchThread {
        registerRerunReport(report, ReporterOpts.PresentInfoProvided)
      }
    }
  
    override def suiteStarting(report: Report) {
      if (report == null)
        throw new NullPointerException("report is null")
      usingEventDispatchThread {
        registerRerunReport(report, ReporterOpts.PresentSuiteStarting)
      }
    }
  
    override def suiteCompleted(report: Report) {
      if (report == null)
        throw new NullPointerException("report is null")
      usingEventDispatchThread {
        registerRerunReport(report, ReporterOpts.PresentSuiteCompleted)
      }
    }
  
    override def suiteAborted(report: Report) {
      if (report == null)
        throw new NullPointerException("report is null")
      usingEventDispatchThread {
        rerunColorBox.setRed()
        registerRerunReport(report, ReporterOpts.PresentSuiteAborted)
      }
    }
  
    override def runStopped() {
      // Create the Report outside of the event handler thread, because otherwise
      // the event handler thread shows up as the originating thread of this report,
      // and that looks bad and is actually wrong.
      val stringToReport: String = Resources("rerunStopped", rerunTestsCompletedCount.toString)
      val report: Report = new Report("org.scalatest.tools.Runner", stringToReport)
      usingEventDispatchThread {
        registerRerunReport(report, ReporterOpts.PresentRunStopped)
      }
    }
  
    override def runAborted(report: Report) {
      if (report == null)
        throw new NullPointerException("report is null")
      usingEventDispatchThread {
        rerunColorBox.setRed()
        registerRerunReport(report, ReporterOpts.PresentRunAborted)
      }
    }
  
    override def runCompleted() {
      // Create the Report outside of the event handler thread, because otherwise
      // the event handler thread shows up as the originating thread of this report,
      // and that looks bad and is actually wrong.
      val stringToReport: String = Resources("rerunCompleted", rerunTestsCompletedCount.toString)
      val report: Report = new Report("org.scalatest.tools.Runner", stringToReport)

      usingEventDispatchThread {
        registerRerunReport(report, ReporterOpts.PresentRunCompleted)
      }
    }
  }

  // Invoked by ReadyState if can't run when the Run or Rerun buttons
  // are pressed. May never happen. If so, delete this. Before it was
  // commented that the problem could occur when they change the prefs.
  def showErrorDialog(title: String, msg: String) {
    val jOptionPane: JOptionPane = new NarrowJOptionPane(msg, JOptionPane.ERROR_MESSAGE)
    val jd: JDialog = jOptionPane.createDialog(RunnerJFrame.this, title)
    jd.show()
  }

  private class RunnerThread extends Thread {

    override def run() {
  
      withClassLoaderAndDispatchReporter(runpathList, reporterSpecs, Some(graphicRunReporter)) {
        (loader, dispatchReporter) => {
          try {
            Runner.doRunRunRunADoRunRun(dispatchReporter, suitesList, stopper, includes, excludes,
                propertiesMap, concurrent, memberOfList, beginsWithList, testNGList, runpathList, loader, RunnerJFrame.this) 
          }
          finally {
            stopper.reset()
          }
        }
      }
    }
  }

  private class RerunnerThread(rerunnable: Rerunnable) extends Thread {

    if (rerunnable == null)
      throw new NullPointerException

    override def run() {
  
      val distributor: Option[Distributor] = None

      withClassLoaderAndDispatchReporter(runpathList, reporterSpecs, Some(graphicRerunReporter)) {
        (loader, dispatchReporter) => {
          try {
            rerunnable.rerun(dispatchReporter, stopper, includes, Runner.excludesWithIgnore(excludes), propertiesMap,
                distributor, loader)
          }
          catch {
            case ex: Throwable => {
              val report: Report = new Report("org.scalatest.tools.Runner", Resources("bigProblems"), Some(ex), None)
              dispatchReporter.runAborted(report)
            }
          }
          finally {
            stopper.reset()
            RunnerJFrame.this.done()
          }
        }
      }
    }
  }
}

private[scalatest] object RunnerJFrame {
  def getTitle(recipeName: Option[String]): String = {
    val scalaTestTitle: String = Resources("ScalaTestTitle")
    recipeName match {
      case Some(rn) => {
        Resources("titleAndFileName", scalaTestTitle, rn)
      }
      case None => scalaTestTitle
    }
  }
}

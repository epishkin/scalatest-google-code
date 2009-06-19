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
import javax.swing.WindowConstants
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.JMenu
import javax.swing.JMenuBar
import javax.swing.JMenuItem
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JEditorPane
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
import org.scalatest.prop.PropertyTestFailedException
import org.scalatest.events._

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
    testNGList: List[String], passFailReporter: Option[Reporter]) extends
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

  // The detailsJEditorPane displays the text details of a report.
  private val detailsJEditorPane: JEditorPane = new JEditorPane("text/html", null)

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

  private var nextRunStamp = 1

  initialize()

  private def initialize() = {

    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE)

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

    detailsJEditorPane.setEditable(false)
    // detailsJEditorPane.setLineWrap(true) TODO: Delete this if staying with JEditorPane, was for JTextArea
    // detailsJEditorPane.setWrapStyleWord(true)
    val detailsJScrollPane: JScrollPane = new JScrollPane(detailsJEditorPane)

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
            detailsJEditorPane.setText("")
            currentState = currentState.listSelectionChanged(RunnerJFrame.this)
          }
          else {

            val report: Report = rh.report
            val reportType: ReporterOpts.Value = rh.reportType
            val isRerun: Boolean = rh.isRerun
  
            val fontSize = reportsJList.getFont.getSize

            val title = 
              if (isRerun)
                Resources("RERUN_" + ReporterOpts.getUpperCaseName(reportType))
              else
                Resources(ReporterOpts.getUpperCaseName(reportType))

            val isFailureReport =
              reportType == ReporterOpts.PresentTestFailed || reportType == ReporterOpts.PresentSuiteAborted ||
                  reportType == ReporterOpts.PresentRunAborted

            val fileAndLineOption: Option[String] =
              report.throwable match {
                case Some(throwable) =>
                  throwable match {
                    case tfe: TestFailedException =>
                      tfe.failedTestCodeFileNameAndLineNumberString 
                    case _ => None
                  }
                case None => None
              }

              val throwableTitle =
                report.throwable match {
                  case Some(throwable) => throwable.getClass.getName
                  case None => Resources("None")
                }

              // Any stack trace elements lower than a TestFailedException's failedTestCodeStackDepth
              // will show up as gray in the displayed stack trace, because those are ScalaTest methods.
              // The rest will show up as black.
              val (grayStackTraceElements, blackStackTraceElements) =
                report.throwable match {
                  case Some(throwable) =>
                    val stackTraceElements = throwable.getStackTrace.toList
                    throwable match {
                      case tfe: TestFailedException =>
                        (stackTraceElements.take(tfe.failedTestCodeStackDepth), stackTraceElements.drop(tfe.failedTestCodeStackDepth))
                      case _ => (List(), stackTraceElements)
                    } 
                  case None => (List(), List())
                }

            def getHTMLForStackTrace(stackTraceList: List[StackTraceElement]) =
              stackTraceList.map((ste: StackTraceElement) => <span>{ ste.toString }</span><br />)

            def getHTMLForCause(throwable: Throwable): scala.xml.NodeBuffer = {
              val cause = throwable.getCause
              if (cause != null) {
                <table>
                <tr valign="top">
                <td align="right"><span class="label">{ Resources("DetailsCause") + ":" }</span></td>
                <td align="left">{ cause.getClass.getName }</td>
                </tr>
                <tr valign="top">
                <td align="right"><span class="label">{ Resources("DetailsMessage") + ":" }</span></td>
                <td align="left"><span>{ if (cause.getMessage != null) cause.getMessage else Resources("None") }</span></td>
                </tr>
                </table>
                <table>
                <tr valign="top">
                <td align="left" colspan="2">{ getHTMLForStackTrace(cause.getStackTrace.toList) }</td>
                </tr>
                </table> &+ getHTMLForCause(cause)
              }
              else new scala.xml.NodeBuffer
            }

            val mainMessage =
              report.throwable match {
                case Some(ex: PropertyTestFailedException) => ex.undecoratedMessage
                case _ => report.message.trim
              }

            val propCheckArgs: List[Any] =
              report.throwable match {
                case Some(ex: PropertyTestFailedException) => ex.args
                case _ => List()
              }

            val propCheckLabels: List[String] =
              report.throwable match {
                case Some(ex: PropertyTestFailedException) => ex.labels
                case _ => List()
              }

            val detailsHTML =
              <html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
                <head>
                  <style type="text/css">
                    body {{ font-family: sans-serif; font-size: { fontSize }pt; }}
                    .label {{ color: #444444; font-weight: bold; }}
                    .gray {{ color: black; }}
                    .dark {{ font-weight: bold; color: #111111; }}
                  </style>
                </head>
                <body>
                  <table>
                  <tr valign="top"><td align="right"><span class="label">{ Resources("DetailsReport") + ":" }</span></td><td align="left"><span>{ title }</span></td></tr>
                  <tr valign="top"><td align="right"><span class="label">{ Resources("DetailsName") + ":" }</span></td><td align="left">{ report.name }</td></tr>
                  {
                    if (report.message.trim.length != 0) {
                      <tr valign="top"><td align="right"><span class="label">{ Resources("DetailsMessage") + ":" }</span></td><td align="left">
                      {
                        if (isFailureReport) {
                          <span class="dark">{ mainMessage }</span>
                        } else {
                          <span>{ mainMessage }</span>
                        }
                      }
                      </td></tr>
                    }
                    else <!-- -->
                  }
                  {
                    fileAndLineOption match {
                      case Some(fileAndLine) =>
                        <tr valign="top"><td align="right"><span class="label">{ Resources("LineNumber") + ":" }</span></td><td align="left"><span class="dark">{ "(" + fileAndLine + ")" }</span></td></tr>
                      case None =>
                    }
                  }
                  {
                    if (!propCheckArgs.isEmpty) {
                        for ((propCheckArg, argIndex) <- propCheckArgs.zipWithIndex) yield
                          <tr valign="top"><td align="right"><span class="label">{ Resources("argN", argIndex.toString) + ":" }</span></td><td align="left"><span class="dark">{ propCheckArg.toString }</span></td></tr>
                    }
                    else new scala.xml.NodeBuffer
                  }
                  {
                    if (!propCheckLabels.isEmpty) {
                      val labelOrLabels = if (propCheckLabels.length > 1) Resources("DetailsLabels") else Resources("DetailsLabel")
                      val labelHTML = for (propCheckLabel <- propCheckLabels) yield {
                        <span class="dark">{ propCheckLabel }</span><br></br>
                      }
                      <tr valign="top"><td align="right"><span class="label">{ labelOrLabels + ":" }</span></td><td align="left"><span class="dark">{ labelHTML }</span></td></tr>
                    }
                    else new scala.xml.NodeBuffer
                  }
                  <tr valign="top"><td align="right"><span class="label">{ Resources("DetailsDate") + ":" }</span></td><td align="left">{ report.date }</td></tr>
                  <tr valign="top"><td align="right"><span class="label">{ Resources("DetailsThread") + ":" }</span></td><td align="left">{ report.threadName }</td></tr>
                  <tr valign="top"><td align="right"><span class="label">{ Resources("DetailsThrowable") + ":" }</span></td><td align="left">{ throwableTitle }</td></tr>
                  </table>
                  <table>
                  <tr valign="top"><td align="left" colspan="2">
                  { grayStackTraceElements.map((ste: StackTraceElement) => <span class="gray">{ ste.toString }</span><br />) }
                  { blackStackTraceElements.map((ste: StackTraceElement) => <span>{ ste.toString }</span><br />) }
                  </td></tr>
                  </table>
                  {
                    report.throwable match {
                      case Some(t) => getHTMLForCause(t)
                      case None =>
                    }
                  }
                </body>
              </html>

            detailsJEditorPane.setText(detailsHTML.toString)
            detailsJEditorPane.setCaretPosition(0)
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
          // Only exit if started from main(), not run(). If starting from run(),
          // we want to return a pass/fail status from run(). Actually, if we
          // have a passFailReporter, then that means we want to indicate status,
          // so that's why it is used here to determine whether or not to exit.
          passFailReporter match {
            case Some(_) =>
            case None => System.exit(0)
          }
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
    detailsJEditorPane.setText("")

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

  private def registerReport(report: Report, reportType: ReporterOpts.Value): ReportHolder = {
    registerRunOrRerunReport(report, reportType, false)
  }

  private def registerRerunReport(report: Report, reportType: ReporterOpts.Value): ReportHolder = {
    registerRunOrRerunReport(report, reportType, true)
  }

  private def registerRunOrRerunReport(report: Report, reportType: ReporterOpts.Value, isRerun: Boolean): ReportHolder = {

    val reportHolder: ReportHolder = new ReportHolder(report, reportType, isRerun)

    if (reportTypesToCollect.contains(reportType)) {
      collectedReports = reportHolder :: collectedReports
      if (viewOptions.contains(reportType)) {
        val shouldAddElement = report match {
          case sr: SpecReport => sr.includeInSpecOutput
          case _ => true
        }
        if (shouldAddElement) reportsListModel.addElement(reportHolder)
      }
    }

    reportHolder
  }

  private class GraphicRunReporter extends Reporter {

    override def apply(event: Event) {
      event match {

        case RunStarting(ordinal, testCount, formatter, payload, threadName, timeStamp) =>

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
  
            detailsJEditorPane.setText("")

            if (viewOptions.contains(ReporterOpts.PresentRunStarting))
              reportsListModel.addElement(reportHolder)
          }

        case RunCompleted(ordinal, duration, summary, formatter, payload, threadName, timeStamp) =>

          // Create the Report outside of the event handler thread, because otherwise
          // the event handler thread shows up as the originating thread of this report,
          // and that looks bad and is wrong to boot.
          val stringToReport: String = Resources("runCompleted", testsCompletedCount.toString)
          val report: Report = new Report("org.scalatest.tools.Runner", stringToReport)
          usingEventDispatchThread {
            registerReport(report, ReporterOpts.PresentRunCompleted)
          }
  
        case RunAborted(ordinal, message, throwable, duration, summary, formatter, payload, threadName, timeStamp) => 

          val report = new Report("org.scalatest.tools.Runner", message, throwable, None)

          usingEventDispatchThread {
            progressBar.setRed()
            registerReport(report, ReporterOpts.PresentRunAborted)
            // Must do this here, not in RunningState.runFinished, because the runFinished
            // invocation can happen before this runCompleted invocation, which means that 
            // the first error in the run may not be in the JList model yet. So must wait until
            // a run completes. I was doing it in runCompleted, which works, but for long runs
            // you must wait a long time for that thing to be selected. Nice if it gets selected
            // right away.
            selectFirstFailureIfExistsAndNothingElseAlreadySelected()
          }

        case RunStopped(ordinal, duration, summary, formatter, payload, threadName, timeStamp) =>

          // Create the Report outside of the event handler thread, because otherwise
          // the event handler thread shows up as the originating thread of this report,
          // and that looks bad and is actually wrong.
          val stringToReport: String = Resources("runStopped", testsCompletedCount.toString)
          val report: Report = new Report("org.scalatest.tools.Runner", stringToReport)
          usingEventDispatchThread {
            registerReport(report, ReporterOpts.PresentRunStopped)
          }

        case SuiteStarting(ordinal, suiteName, suiteClassName, formatter, rerunnable, payload, threadName, timeStamp) =>

          // TODO: Oh, this kills the formatting too. Because that was being done with SpecReports. Will need to 
          // fix that in the GUI. And the way to do it is store events. But that's a later step.
          val report: Report = new Report(suiteName, "suite starting, dude", None, rerunnable)

          usingEventDispatchThread {
            registerReport(report, ReporterOpts.PresentSuiteStarting)
          }
  
        case SuiteCompleted(ordinal, suiteName, suiteClassName, duration, formatter, rerunnable, payload, threadName, timeStamp) => 
  
          val report: Report = new Report(suiteName, "suite completed, dude", None, rerunnable)

          usingEventDispatchThread {
            registerReport(report, ReporterOpts.PresentSuiteCompleted)
          }

        case SuiteAborted(ordinal, message, suiteName, suiteClassName, throwable, duration, formatter, rerunnable, payload, threadName, timeStamp) => 

          val report: Report = new Report(suiteName, message, throwable, rerunnable)

          usingEventDispatchThread {
            progressBar.setRed()
            registerReport(report, ReporterOpts.PresentSuiteAborted)
            // Must do this here, not in RunningState.runFinished, because the runFinished
            // invocation can happen before this runCompleted invocation, which means that 
            // the first error in the run may not be in the JList model yet. So must wait until
            // a run completes. I was doing it in runCompleted, which works, but for long runs
            // you must wait a long time for that thing to be selected. Nice if it gets selected
            // right away.
            selectFirstFailureIfExistsAndNothingElseAlreadySelected()
          }

        case TestStarting(ordinal, suiteName, suiteClassName, testName, formatter, rerunnable, payload, threadName, timeStamp) =>
  
          val report: Report = new Report(suiteName + ": " + testName, "test starting, dude", None, rerunnable)

          usingEventDispatchThread {
            registerReport(report, ReporterOpts.PresentTestStarting)
          }

        case TestIgnored(ordinal, suiteName, suiteClassName, testName, formatter, payload, threadName, timeStamp) => 

          val report: Report = new Report(suiteName + ": " + testName, "test ignored, dude", None, None)

          usingEventDispatchThread {
            registerReport(report, ReporterOpts.PresentTestIgnored)
          }
  
        case TestSucceeded(ordinal, suiteName, suiteClassName, testName, duration, formatter, rerunnable, payload, threadName, timeStamp) => 
  
          val report: Report = new Report(suiteName + ": " + testName, "test succeeded, dude", None, rerunnable)

          usingEventDispatchThread {
            testsCompletedCount += 1
            statusJPanel.setTestsRun(testsCompletedCount, true)
            progressBar.setValue(testsCompletedCount)
            registerReport(report, ReporterOpts.PresentTestSucceeded)
          }
  
        case TestFailed(ordinal, message, suiteName, suiteClassName, testName, throwable, duration, formatter, rerunnable, payload, threadName, timeStamp) => 

          val report: Report = new Report(suiteName + ": " + testName, message, throwable, rerunnable)

          usingEventDispatchThread {
            testsCompletedCount += 1
            // Passing in false here increments the test failed count
            // in the statusJPanel, which updates the counter on the GUI
            statusJPanel.setTestsRun(testsCompletedCount, false)
            progressBar.setValue(testsCompletedCount)
            progressBar.setRed()
            registerReport(report, ReporterOpts.PresentTestFailed)
            // Must do this here, not in RunningState.runFinished, because the runFinished
            // invocation can happen before this runCompleted invocation, which means that 
            // the first error in the run may not be in the JList model yet. So must wait until
            // a run completes. I was doing it in runCompleted, which works, but for long runs
            // you must wait a long time for that thing to be selected. Nice if it gets selected
            // right away.
            selectFirstFailureIfExistsAndNothingElseAlreadySelected()
          }

        case InfoProvided(ordinal, message, nameInfo, throwable, formatter, payload, threadName, timeStamp) =>

          val report: Report = new Report("some name", "info provided, dude", throwable, None)

          usingEventDispatchThread {
            registerReport(report, ReporterOpts.PresentInfoProvided)
          }

        case _ =>
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
  override def rerunFromGUI(rerunnable: Rerunner) {
    (new RerunnerThread(rerunnable)).start()
  }

  // This must be called by the event handler thread
  def prepUIForRunning() {
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
    detailsJEditorPane.setText("")
  }

  // This must be called by the event handler thread
  def prepUIWhileRunning() {
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
    val runText: String = Resources("Run")
    val stopText: String = Resources("Stop")
    runJButton.setText(runText)
    rerunJButton.setText(stopText)
    runJButton.setEnabled(false)
    rerunJButton.setEnabled(true)
    rerunColorBox.setGray()

    // Clear the selection, so it can scroll to an error
    reportsJList.clearSelection() 
  }

  // This must be called by the event handler thread
  def prepUIWhileRerunning() {
    val runText: String = Resources("Run")
    val stopText: String = Resources("Stop")
    runJButton.setText(runText)
    rerunJButton.setText(stopText)
    runJButton.setEnabled(false)
    rerunJButton.setEnabled(true)
  }

  // This must be called by the event handler thread
  def prepUIForReady() {
    val runText: String = Resources("Run")
    val rerunText: String = Resources("Rerun")
    runJButton.setText(runText)
    rerunJButton.setText(rerunText)
    runJButton.setEnabled(true)
    val rh: ReportHolder = reportsJList.getSelectedValue.asInstanceOf[ReportHolder] 
    rerunJButton.setEnabled(rh != null && rh.report.rerunnable.isDefined)
  }

  // This must be called by the event handler thread
  def prepUIForStopping() {
    val stopText: String = Resources("Stop")
    val rerunText: String = Resources("Rerun")
    runJButton.setText(stopText)
    rerunJButton.setText(rerunText)
    runJButton.setEnabled(false)
    rerunJButton.setEnabled(false)
  }

  // This must be called by the event handler thread
  def prepUIForReStopping() {
    val runText: String = Resources("Run")
    val stopText: String = Resources("Stop")
    runJButton.setText(runText)
    rerunJButton.setText(stopText)
    runJButton.setEnabled(false)
    rerunJButton.setEnabled(false)
  }

  private def getModelAsList: List[ReportHolder] = {
    val model = reportsJList.getModel
    val listBuf = new scala.collection.mutable.ListBuffer[ReportHolder]
    for (i <- 0 until model.getSize) {
      listBuf += model.getElementAt(i).asInstanceOf[ReportHolder]
    }
    listBuf.toList
  }

  private def isFailureReport(reportHolder: ReportHolder) = {
    val reportType = reportHolder.reportType
    reportType == ReporterOpts.PresentTestFailed || reportType == ReporterOpts.PresentRunAborted || 
        reportType == ReporterOpts.PresentSuiteAborted
  }

  // This must be called by the event handler thread
  /*
  After a rerun, there will usually always be a selected item,
  which is what was selected to rerun. If the rerun resulted in
  an error, it would be nice to select that first error and scroll down.
  So this will do that, which means it doesn't care if something is
  already selected. It will always select the first error in the
  last rerun if one exists. This is called as errors come in during
  a rerun. The error's reportHolder is passed. It will be selected only
  if it is the first error in the last rerun. Any other time this method will
  do nothing. The reason is that reruns can take a while, and the user may be
  selecting and exploring the results as it runs. So I don't want to keep forcing
  a different selection. Only the first time an error comes in in a rerun will it happen.
  (During a run, the first error will be selected only if there is no other selection. But
  here it happens even if something else is selected, because during a rerun normally the
  thing you wanted to rerun will already be selected.)
  */
  private def selectFirstErrorInLastRerunIfThisIsThatError(candidateReportHolder: ReportHolder) {

    // First get the model into a List
    val modelList = getModelAsList

    if (modelList.exists(_.isRerun)) {
      val listOfReportsForLastRerunExcludingRunStarting =
        modelList.reverse.takeWhile(reportHolder => reportHolder.isRerun && (reportHolder.reportType != ReporterOpts.PresentRunStarting))
      val firstTestFailedReportInLastRerun =
        listOfReportsForLastRerunExcludingRunStarting.reverse.find(isFailureReport(_))
      firstTestFailedReportInLastRerun match {
        case Some(reportHolder) =>
          if (reportHolder == candidateReportHolder) // Only select it if the one passed is the first one
            reportsJList.setSelectedValue(reportHolder, true)
        case None => // do nothing if no failure reports in last rerun
      }
    }
  }

  private def scrollTheRerunStartingReportToTheTopOfVisibleReports() {

    def indexOfRunStartingReportForLastRerunOption: Option[Int] = {
      var i = reportsListModel.getSize - 1
      var found = false
      while (i >= 0 && !found) {
        val rh = reportsListModel.getElementAt(i).asInstanceOf[ReportHolder]
        if (rh.reportType == ReporterOpts.PresentRunStarting) {
          found = true
        }
        if (!found) i -= 1
      }
      if (found) Some(i) else None
    }

    val selectedReportHandler = reportsJList.getSelectedValue.asInstanceOf[ReportHolder]

    if (selectedReportHandler == null || selectedReportHandler.reportType == ReporterOpts.PresentRunStarting) { // only scroll if there's no selection, which means no error happened

      val firstVisibleIndex = reportsJList.getFirstVisibleIndex
      val lastVisibleIndex = reportsJList.getLastVisibleIndex

      if (lastVisibleIndex > firstVisibleIndex) { // should always be true, but this is better than an assert because things will keep going

        val numCellsVisible = lastVisibleIndex - firstVisibleIndex

        val indexOfLastReport = reportsListModel.getSize - 1

        indexOfRunStartingReportForLastRerunOption match {
          case Some(indexOfRunStartingReportForLastRerun) =>

            val indexToEnsureIsVisible =
              if (indexOfRunStartingReportForLastRerun + numCellsVisible < indexOfLastReport) 
                indexOfRunStartingReportForLastRerun + numCellsVisible
              else
                indexOfLastReport

            reportsJList.ensureIndexIsVisible(indexToEnsureIsVisible)

            // Select one report after the rerun starting report, if it is a test starting, test succeeded, or suite starting report,
            // because this should be the one they requested was rerun. So that's the most intuitive one to select
            // after a run if there was no error. (Test succeeded is possible because Spec's will send SpecReports that
            // say not to display test starting reports.)
            val indexOfSecondReportInRerun = indexOfRunStartingReportForLastRerun + 1
            if (indexOfSecondReportInRerun <= indexOfLastReport) { // Should always be true, but an if is better than an assert

              val firstReportAfterRerunStarting = reportsListModel.getElementAt(indexOfSecondReportInRerun).asInstanceOf[ReportHolder]
              if (firstReportAfterRerunStarting.reportType == ReporterOpts.PresentTestStarting ||
                  firstReportAfterRerunStarting.reportType == ReporterOpts.PresentSuiteStarting ||
                  firstReportAfterRerunStarting.reportType == ReporterOpts.PresentTestSucceeded) {
                reportsJList.setSelectedIndex(indexOfSecondReportInRerun)
              }
              // If they have display only Runs and Failures selected, it won't show successful tests. In that case
              // just select the run starting report.
              else reportsJList.setSelectedIndex(indexOfRunStartingReportForLastRerun)
            }
          case None =>
        }
      }
    }
  }

  private def ensureLastRerunReportIsVisibleIfFirstRerunReportStaysVisibleToo(lastRerunReportHolder: ReportHolder) {

/*
    def indexOfRunStartingReportForLastRerun: Option[Int] = {
      var i = reportsListModel.getSize - 1
      var found = false
      while (i >= 0 && !found) {
        val rh = reportsListModel.getElementAt(i).asInstanceOf[ReportHolder]
        if (rh.reportType == ReporterOpts.PresentRunStarting) {
          found = true
        }
        if (!found) i -= 1
      }
      if (found) Some(i) else None
    }

    val firstVisibleIndex = reportsJList.getFirstVisibleIndex
    val lastVisibleIndex = reportsJList.getLastVisibleIndex

    if (lastVisibleIndex > firstVisibleIndex) { // should always be true, but this is better than an assert because things will keep going
      // Make this one less than what's actually visible, maybe change this after testing
      val numCellsVisible = lastVisibleIndex - firstVisibleIndex

      val indexOfLastReport = reportsListModel.getSize - 1

      val indexOfFirstVisibleReportIfLastReportAlsoVisible = indexOfLastReport - numCellsVisible // not sure about the math yet

      if (lastRerunReportHolder == reportsListModel.get(indexOfLastReport)) { // Should always be true
        indexOfRunStartingReportForLastRerun match {
          case Some(index) =>
            if (index >= indexOfFirstVisibleReportIfLastReportAlsoVisible)
              reportsJList.ensureIndexIsVisible(indexOfLastReport)
          case None =>
        }
      }
    }
*/
  }


/* TODO: Delete this code
  // This must be called by the event handler thread
  private def scrollRerunReportsIfNeedBe() {

    def rerunStartingOrFirstRerunErrorReportIsSelected: Boolean = {

      val selectedReportHolder: ReportHolder = reportsJList.getSelectedValue.asInstanceOf[ReportHolder] 

      if (selectedReportHolder != null) {

        val modelList = getModelAsList // First get the model into a List

        if (modelList.exists(_.isRerun)) {

          val reversedModelList = modelList.reverse

          // Check to see if the run starting report for the last rerun is currently selected
          val lastRunStartingReportHolderOption = 
            reversedModelList.find(reportHolder => reportHolder.isRerun && (reportHolder.reportType == ReporterOpts.PresentRunStarting))

          lastRunStartingReportHolderOption match {

            case Some(lastRunStartingReportHolder) => selectedReportHolder == lastRunStartingReportHolder

            case None => // Check to see if the first error in the last rerun is currently selected

              val listOfReportsForLastRerunExcludingRunStarting =
                reversedModelList.takeWhile(reportHolder => reportHolder.isRerun && (reportHolder.reportType != ReporterOpts.PresentRunStarting))

              val firstFailureReportInLastRerun =
                listOfReportsForLastRerunExcludingRunStarting.reverse.find(isFailureReport(_))

              firstFailureReportInLastRerun match {
                case Some(failureReportHolder) => selectedReportHolder == failureReportHolder
                case None => false
              }
          }
        }
        else false // no rerun reports at all
      }
      else false // nothing is selected at all
    }

    /*
    the scrollIfNeedBe would only work if the first error in the rerun, or rerun starting of the last rerun is selected.
    If so, then it will check to make sure rerun starting of the last rerun is visible. If not, it does nothing, because
    that means the user has gone in and scrolled or selected something. But if not, then it will scroll to see the last
    report that has just come in, so long as that doesn't scroll the rerun starting or first selected error off the top.
    Oh, and all this stuff should probably be disabled for now if running in concurrent mode, because things come
    in out of order.
    */
    val firstVisibleIndex = reportsJList.getFirstVisibleIndex
    val lastVisibleIndex = reportsJList.getLastVisibleIndex
    if (lastVisibleIndex > firstVisibleIndex) { // should always be true, but this is better than an assert because things will keep going
      // Make this one less than what's actually visible
      val numCellsVisible = lastVisibleIndex - firstVisibleIndex
      val indexOfCurrentSelection = reportsJList.getSelectedIndex
    }
  }
*/

  // This must be called by the event handler thread
  private def selectFirstFailureIfExistsAndNothingElseAlreadySelected() {

    val rh: ReportHolder = reportsJList.getSelectedValue.asInstanceOf[ReportHolder] 

    if (rh == null) { // Only do this if something isn't already selected

      // First get the model into a List
      val modelList = getModelAsList

      val firstFailureReport = modelList.find(isFailureReport(_))
      firstFailureReport match {
        case Some(reportHolder) => reportsJList.setSelectedValue(reportHolder, true)
        case None => // do nothing if no failure reports in the run
      }
    }
  }

  def getSelectedRerunner(): Option[Rerunner] = {
    val rh: ReportHolder = reportsJList.getSelectedValue().asInstanceOf[ReportHolder] 
    if (rh == null)
      None
    else
      rh.report.rerunnable
  }

  private class GraphicRerunReporter extends Reporter {

    // This is written by the event handler thread to avoid having the event handler thread spend time
    // determining if an error has previously occurred by looking through the reports. This way if a
    // rerun has a lot of errors, you don't hang up the GUI giving the event handler thread too much
    // work to do.
    var anErrorHasOccurredAlready = false

    def apply(event: Event) {

      event match {
        case RunStarting(ordinal, testCount, formatter, payload, threadName, timeStamp) =>

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
            anErrorHasOccurredAlready = false;
          }

        case RunCompleted(ordinal, duration, summary, formatter, payload, threadName, timeStamp) =>

          // Create the Report outside of the event handler thread, because otherwise
          // the event handler thread shows up as the originating thread of this report,
          // and that looks bad and is actually wrong.
          val stringToReport: String = Resources("rerunCompleted", rerunTestsCompletedCount.toString)
          val report: Report = new Report("org.scalatest.tools.Runner", stringToReport)

          usingEventDispatchThread {
            registerRerunReport(report, ReporterOpts.PresentRunCompleted)
            scrollTheRerunStartingReportToTheTopOfVisibleReports()
          }
  
        case RunAborted(ordinal, message, throwable, duration, summary, formatter, payload, threadName, timeStamp) => 

          val report = new Report("org.scalatest.tools.Runner", message, throwable, None)

          usingEventDispatchThread {
            rerunColorBox.setRed()
            val reportHolder = registerRerunReport(report, ReporterOpts.PresentRunAborted)
            if (!anErrorHasOccurredAlready) {
              selectFirstErrorInLastRerunIfThisIsThatError(reportHolder)
              anErrorHasOccurredAlready = true
            }
          }

        case RunStopped(ordinal, duration, summary, formatter, payload, threadName, timeStamp) =>
  
          // Create the Report outside of the event handler thread, because otherwise
          // the event handler thread shows up as the originating thread of this report,
          // and that looks bad and is actually wrong.
          val stringToReport: String = Resources("rerunStopped", rerunTestsCompletedCount.toString)
          val report: Report = new Report("org.scalatest.tools.Runner", stringToReport)
          usingEventDispatchThread {
            registerRerunReport(report, ReporterOpts.PresentRunStopped)
            scrollTheRerunStartingReportToTheTopOfVisibleReports()
          }

        case SuiteStarting(ordinal, suiteName, suiteClassName, formatter, rerunnable, payload, threadName, timeStamp) =>

          val report: Report = new Report(suiteName, "suite starting, dude", None, rerunnable)

          usingEventDispatchThread {
            registerRerunReport(report, ReporterOpts.PresentSuiteStarting)
          }
  
        case SuiteCompleted(ordinal, suiteName, suiteClassName, duration, formatter, rerunnable, payload, threadName, timeStamp) => 
  
          val report: Report = new Report(suiteName, "suite completed, dude", None, rerunnable)

          usingEventDispatchThread {
            registerRerunReport(report, ReporterOpts.PresentSuiteCompleted)
          }

        case SuiteAborted(ordinal, message, suiteName, suiteClassName, throwable, duration, formatter, rerunnable, payload, threadName, timeStamp) => 

          val report: Report = new Report(suiteName, message, throwable, rerunnable)

          usingEventDispatchThread {
            rerunColorBox.setRed()
            val reportHolder = registerRerunReport(report, ReporterOpts.PresentSuiteAborted)
            if (!anErrorHasOccurredAlready) {
              selectFirstErrorInLastRerunIfThisIsThatError(reportHolder)
              anErrorHasOccurredAlready = true
            }
          }
 
        case TestStarting(ordinal, suiteName, suiteClassName, testName, formatter, rerunnable, payload, threadName, timeStamp) =>

          val report: Report = new Report(suiteName + ": " + testName, "test starting, dude", None, rerunnable)

          usingEventDispatchThread {
            registerRerunReport(report, ReporterOpts.PresentTestStarting)
          }
  
        case TestIgnored(ordinal, suiteName, suiteClassName, testName, formatter, payload, threadName, timeStamp) => 

          val report: Report = new Report(suiteName + ": " + testName, "test ignored, dude", None, None)

          usingEventDispatchThread {
            rerunColorBox.setValue(rerunTestsCompletedCount)
            registerRerunReport(report, ReporterOpts.PresentTestIgnored)
          }

        case TestSucceeded(ordinal, suiteName, suiteClassName, testName, duration, formatter, rerunnable, payload, threadName, timeStamp) => 

          val report: Report = new Report(suiteName + ": " + testName, "test succeeded, dude", None, rerunnable)

          usingEventDispatchThread {
            rerunTestsCompletedCount += 1
            rerunColorBox.setValue(rerunTestsCompletedCount)
            registerRerunReport(report, ReporterOpts.PresentTestSucceeded)
          }

        case TestFailed(ordinal, message, suiteName, suiteClassName, testName, throwable, duration, formatter, rerunnable, payload, threadName, timeStamp) => 

          val report: Report = new Report(suiteName + ": " + testName, "test failed, dude", throwable, rerunnable)

          usingEventDispatchThread {
            rerunTestsCompletedCount += 1
            rerunColorBox.setValue(rerunTestsCompletedCount)
            rerunColorBox.setRed()
            val reportHolder = registerRerunReport(report, ReporterOpts.PresentTestFailed)
            if (!anErrorHasOccurredAlready) {
              selectFirstErrorInLastRerunIfThisIsThatError(reportHolder)
              anErrorHasOccurredAlready = true
            }
          }
  
        case InfoProvided(ordinal, message, nameInfo, throwable, formatter, payload, threadName, timeStamp) =>

          val report: Report = new Report("some name", "info provided, dude", throwable, None)

          usingEventDispatchThread {
            registerRerunReport(report, ReporterOpts.PresentInfoProvided)
          }

        case _ =>
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
  
      withClassLoaderAndDispatchReporter(runpathList, reporterSpecs, Some(graphicRunReporter), passFailReporter) {
        (loader, dispatchReporter) => {
          try {
            Runner.doRunRunRunADoRunRun(dispatchReporter, suitesList, stopper, includes, excludes,
                propertiesMap, concurrent, memberOfList, beginsWithList, testNGList, runpathList, loader, RunnerJFrame.this, nextRunStamp) 
          }
          finally {
            stopper.reset()
            nextRunStamp += 1
          }
        }
      }
    }
  }

  private class RerunnerThread(rerun: Rerunner) extends Thread {

    if (rerun == null)
      throw new NullPointerException

    override def run() {
  
      val distributor: Option[Distributor] = None

      val tracker = new Tracker(new Ordinal(nextRunStamp))

      withClassLoaderAndDispatchReporter(runpathList, reporterSpecs, Some(graphicRerunReporter), None) {
        (loader, dispatchReporter) => {
          try {
            rerun(dispatchReporter, stopper, includes, Runner.excludesWithIgnore(excludes), propertiesMap,
                distributor, tracker, loader)
          }
          catch {
            case e: Throwable => {
              dispatchReporter.apply(RunAborted(tracker.nextOrdinal(), Resources.bigProblems(e), Some(e)))
            }
          }
          finally {
            stopper.reset()
            RunnerJFrame.this.done()
            nextRunStamp += 1
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

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

import javax.swing._
import java.awt._
import java.net.URL
import javax.swing.border.EmptyBorder
import org.scalatest.events.IndentedText

/**
 * A ListCellRenderer for the event List in the GUI.
 *
 * @author Bill Venners
 */
private[scalatest] class IconEmbellishedListCellRenderer extends ListCellRenderer {

  private val defaultRenderer: DefaultListCellRenderer = new DefaultListCellRenderer()

  private val DEEP_RED: Color = new Color(0xEE, 0x55, 0x66)
  private val UNCOMFORTABLE_GRAY: Color = new Color(0xaf, 0xaf, 0x9f)
  private val BACKGROUND_BLUE: Color = new Color(0x45, 0x76, 0xd4)

  private val myClassLoader: ClassLoader = classOf[IconEmbellishedListCellRenderer].getClassLoader

  private object Icons {

    // Unselected icon URLs
    private val purpleURL: URL = myClassLoader.getResource("images/purpledot.gif")
    private val greenURL: URL = myClassLoader.getResource("images/greendot.gif")
    private val redURL: URL = myClassLoader.getResource("images/reddot.gif")
    private val blueURL: URL = myClassLoader.getResource("images/bluedot.gif")
    private val grayURL: URL = myClassLoader.getResource("images/graydot.gif")
    private val cyanURL: URL = myClassLoader.getResource("images/cyandot.gif")
    private val yellowURL: URL = myClassLoader.getResource("images/yellowdot.gif")
  
    // Selected icon URLs
    private val purpleSelURL: URL = myClassLoader.getResource("images/purpledotsel.gif")
    private val greenSelURL: URL = myClassLoader.getResource("images/greendotsel.gif")
    private val redSelURL: URL = myClassLoader.getResource("images/reddotsel.gif")
    private val blueSelURL: URL = myClassLoader.getResource("images/bluedotsel.gif")
    private val graySelURL: URL = myClassLoader.getResource("images/graydotsel.gif")
    private val cyanSelURL: URL = myClassLoader.getResource("images/cyandotsel.gif")
    private val yellowSelURL: URL = myClassLoader.getResource("images/yellowdotsel.gif")
  
    // Unselected icon images
    private val purpleImageIcon: ImageIcon = new ImageIcon(purpleURL)
    private val greenImageIcon: ImageIcon = new ImageIcon(greenURL)
    private val redImageIcon: ImageIcon = new ImageIcon(redURL)
    private val blueImageIcon: ImageIcon = new ImageIcon(blueURL)
    private val grayImageIcon: ImageIcon = new ImageIcon(grayURL)
    private val cyanImageIcon: ImageIcon = new ImageIcon(cyanURL)
    private val yellowImageIcon: ImageIcon = new ImageIcon(yellowURL)
  
    // Selected icon images
    private val purpleSelImageIcon: ImageIcon = new ImageIcon(purpleSelURL)
    private val greenSelImageIcon: ImageIcon = new ImageIcon(greenSelURL)
    private val redSelImageIcon: ImageIcon = new ImageIcon(redSelURL)
    private val blueSelImageIcon: ImageIcon = new ImageIcon(blueSelURL)
    private val graySelImageIcon: ImageIcon = new ImageIcon(graySelURL)
    private val cyanSelImageIcon: ImageIcon = new ImageIcon(cyanSelURL)
    private val yellowSelImageIcon: ImageIcon = new ImageIcon(yellowSelURL)

    val runStartingIcon = grayImageIcon
    val testStartingIcon = purpleImageIcon
    val testSucceededIcon = greenImageIcon
    val testIgnoredIcon = yellowImageIcon
    val testFailedIcon = redImageIcon
    val suiteStartingIcon = cyanImageIcon
    val suiteCompletedIcon = cyanImageIcon
    val suiteAbortedIcon = redImageIcon
    val infoProvidedIcon = blueImageIcon
    val runStoppedIcon = grayImageIcon
    val runAbortedIcon = redImageIcon
    val runCompletedIcon = grayImageIcon

    val runStartingSelIcon = graySelImageIcon
    val testStartingSelIcon = purpleSelImageIcon
    val testSucceededSelIcon = greenSelImageIcon
    val testIgnoredSelIcon = yellowSelImageIcon
    val testFailedSelIcon = redSelImageIcon
    val suiteStartingSelIcon = cyanSelImageIcon
    val suiteCompletedSelIcon = cyanSelImageIcon
    val suiteAbortedSelIcon = redSelImageIcon
    val infoProvidedSelIcon = blueSelImageIcon
    val runStoppedSelIcon = graySelImageIcon
    val runAbortedSelIcon = redSelImageIcon
    val runCompletedSelIcon = graySelImageIcon
  }

  private def setRendererFont(renderer: JLabel, color: Color) {
    val font: Font = renderer.getFont()
    renderer.setFont(new Font(font.getFontName(), Font.BOLD, font.getSize()))
    renderer.setForeground(color)
  }

  def getListCellRendererComponent(list: JList, value: Object, index: Int, isSelected: Boolean,
      cellHasFocus: Boolean): Component = {

    val renderer: JLabel =
      defaultRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus).asInstanceOf[JLabel] 

    // Setting to a specific background color because that color was used to make icons that
    // look nice when the row is selected. 
    if (isSelected)
      renderer.setBackground(BACKGROUND_BLUE)

    val eventType: ReporterOpts.Value = value.asInstanceOf[EventHolder].eventType

    eventType match {
      case ReporterOpts.PresentRunStarting => {
        if (isSelected)
          renderer.setIcon(Icons.runStartingSelIcon)
        else
          renderer.setIcon(Icons.runStartingIcon)
      }
      case ReporterOpts.PresentTestStarting => {
        if (isSelected)
          renderer.setIcon(Icons.testStartingSelIcon)
        else
          renderer.setIcon(Icons.testStartingIcon)
      }
      case ReporterOpts.PresentTestSucceeded => {
        if (isSelected)
          renderer.setIcon(Icons.testSucceededSelIcon)
        else
          renderer.setIcon(Icons.testSucceededIcon)
      }
      case ReporterOpts.PresentTestIgnored => {
        if (isSelected)
          renderer.setIcon(Icons.testIgnoredSelIcon)
        else
          renderer.setIcon(Icons.testIgnoredIcon)
        setRendererFont(renderer, UNCOMFORTABLE_GRAY)
      }
      case ReporterOpts.PresentTestFailed => {
        if (isSelected)
          renderer.setIcon(Icons.testFailedSelIcon)
        else
          renderer.setIcon(Icons.testFailedIcon)
        setRendererFont(renderer, DEEP_RED)
      }
      case ReporterOpts.PresentRunAborted => {
        if (isSelected)
          renderer.setIcon(Icons.runAbortedSelIcon)
        else
          renderer.setIcon(Icons.runAbortedIcon)
        setRendererFont(renderer, DEEP_RED)
      }
      case ReporterOpts.PresentSuiteAborted => {
        if (isSelected)
          renderer.setIcon(Icons.suiteAbortedSelIcon)
        else
          renderer.setIcon(Icons.suiteAbortedIcon)
        setRendererFont(renderer, DEEP_RED)
      }
      case ReporterOpts.PresentSuiteStarting => {
        if (isSelected)
          renderer.setIcon(Icons.suiteStartingSelIcon)
        else
          renderer.setIcon(Icons.suiteStartingIcon)
      }
      case ReporterOpts.PresentSuiteCompleted => {
        if (isSelected)
          renderer.setIcon(Icons.suiteCompletedSelIcon)
        else
          renderer.setIcon(Icons.suiteCompletedIcon)
      }
      case ReporterOpts.PresentInfoProvided => {
        if (isSelected)
          renderer.setIcon(Icons.infoProvidedSelIcon)
        else
          renderer.setIcon(Icons.infoProvidedIcon)
      }
      case ReporterOpts.PresentRunCompleted => {
        if (isSelected)
          renderer.setIcon(Icons.runCompletedSelIcon)
        else
          renderer.setIcon(Icons.runCompletedIcon)
      }
      case ReporterOpts.PresentRunStopped => {
        if (isSelected)
          renderer.setIcon(Icons.runStoppedSelIcon)
        else
          renderer.setIcon(Icons.runStoppedIcon)
      }
    }

    val event = value.asInstanceOf[EventHolder].event
 
    event.formatter match {

      case Some(IndentedText(_, _, indentationLevel)) =>

        if (indentationLevel > 0) {
          val panel = new JPanel(new BorderLayout)
          panel.setBackground(renderer.getBackground)
          val WidthOfIconInPixels = 12
          panel.setBorder(new EmptyBorder(0, WidthOfIconInPixels * indentationLevel, 0, 0))
          renderer.setBorder(new EmptyBorder(0, 0, 0, 0))
          panel.add(renderer, BorderLayout.CENTER)
          panel
        }
        else renderer

      case _ =>
        renderer
    }
  }
}


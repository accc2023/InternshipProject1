import kotlinx.coroutines.*
import java.awt.BorderLayout
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.nio.file.Path
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import kotlin.io.path.Path
import javax.swing.border.EmptyBorder
import javax.swing.SwingUtilities.invokeLater
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import searchForTextOccurrences

class SearchPanel(private val project: Project) : JPanel(BorderLayout()), Disposable {

    private val dirField = JTextField().apply { toolTipText = "Full directory path" }
    private val queryField = JTextField().apply { toolTipText = "Text to search" }
    private val startBtn = JButton("Start search")
    private val cancelBtn = JButton("Cancel search").apply { isEnabled = false }
    private val statusLabel = JLabel("Idle")

    private val listModel = DefaultListModel<String>()
    private val resultsList = JList(listModel).apply {
        visibleRowCount = 16
        prototypeCellValue = "path/File.kt: 123:456"
    }
    private val scrollPane = JScrollPane(resultsList)

    // Create a coroutine scope with SupervisorJob to ensure each can run/crash without affecting the others
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var currentJob: Job? = null
    private var resultCount = 0

    init {
        border = EmptyBorder(8, 8, 8, 8)

        val form = JPanel()
        form.layout = BoxLayout(form, BoxLayout.Y_AXIS)

        fun labeled(label: String, field: JComponent): JPanel {
            val p = JPanel(BorderLayout(8, 8))
            p.add(JLabel(label), BorderLayout.WEST)
            p.add(field, BorderLayout.CENTER)
            p.border = EmptyBorder(0, 0, 6, 0)
            return p
        }

        val buttons = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.X_AXIS)
            add(startBtn)
            add(Box.createHorizontalStrut(8))
            add(cancelBtn)
            add(Box.createHorizontalGlue())
            add(statusLabel)
        }

        form.add(labeled("Directory:", dirField))
        form.add(labeled("Search for:", queryField))
        form.add(buttons)

        add(form, BorderLayout.NORTH)
        add(scrollPane, BorderLayout.CENTER)

        // UI
        val enableStartIfValid = {
            startBtn.isEnabled = dirField.text.isNotBlank() && queryField.text.isNotBlank() && currentJob == null
        }
        val docListener = object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent?) = enableStartIfValid()
            override fun removeUpdate(e: DocumentEvent?) = enableStartIfValid()
            override fun changedUpdate(e: DocumentEvent?) = enableStartIfValid()
        }
        dirField.document.addDocumentListener(docListener)
        queryField.document.addDocumentListener(docListener)

        // Enter start
        val enterToStart = object : KeyAdapter() {
            override fun keyPressed(e: KeyEvent) {
                if (e.keyCode == KeyEvent.VK_ENTER && startBtn.isEnabled) startBtn.doClick()
            }
        }
        dirField.addKeyListener(enterToStart)
        queryField.addKeyListener(enterToStart)

        startBtn.addActionListener { startSearch() }
        cancelBtn.addActionListener { cancelSearch() }
    }

    private fun startSearch() {
        val dirText = dirField.text.trim()
        val query = queryField.text

        val dirPath: Path = try {
            Path(dirText)
        } catch (e: Exception) {
            showError("Invalid directory path.")
            return
        }

        // Clear the UI
        listModel.clear()
        resultCount = 0
        statusLabel.text = "Searching…"
        startBtn.isEnabled = false
        cancelBtn.isEnabled = true
        dirField.isEnabled = false
        queryField.isEnabled = false

        currentJob = scope.launch {
            try {
                // Use function from task two
                searchForTextOccurrences(query, dirPath).collect { occ ->
                    // Update UI as results stream in
                    invokeLater {
                        listModel.addElement("${occ.file}: ${occ.line}:${occ.offset}")
                        resultCount++
                        statusLabel.text = "Found: $resultCount"
                    }
                }
                invokeLater {
                    statusLabel.text = "Done. Total: $resultCount"
                    finishSearchState()
                }
            } catch (e: Exception) {
                invokeLater {
                    showError("Error: ${e.message ?: e::class.simpleName}")
                    statusLabel.text = "Idle"
                    finishSearchState()
                }
            }
        }
    }

    private fun cancelSearch() {
        currentJob?.cancel()
    }

    private fun finishSearchState() {
        currentJob = null
        cancelBtn.isEnabled = false
        startBtn.isEnabled = dirField.text.isNotBlank() && queryField.text.isNotBlank()
        dirField.isEnabled = true
        queryField.isEnabled = true
    }

    private fun showError(msg: String) {
        JOptionPane.showMessageDialog(this, msg, "Text Search", JOptionPane.ERROR_MESSAGE)
    }

    override fun dispose() {
        scope.cancel()
    }
}
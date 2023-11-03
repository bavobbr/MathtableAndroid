package app.mathtable

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.GridLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity


class GameActivity : AppCompatActivity() {
    private var completed: Boolean = false
    private lateinit var gameGrid: GridLayout
    private val solvedTiles = Array(10) { Array(10) { false } } // 10x10 grid of solved states
    private var suggestedTile: Button? = null
    private var correctAnswersCount = 0
    private val targetCorrectAnswers = 5
    private val margin = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        gameGrid = findViewById(R.id.gameGrid)
        initializeGameGrid()
    }

    private fun initializeGameGrid() {
        // Adjust the grid layout to account for headers
        val totalRows = 11 // 10 for the numbers + 1 for headers
        val totalCols = 11 // 10 for the numbers + 1 for headers
        gameGrid.rowCount = totalRows
        gameGrid.columnCount = totalCols

        // Create row headers
        for (row in 1 until totalRows) {
            val headerText = if (row == 0) "" else (row).toString()
            val textView = createHeaderTextView(headerText)
            gameGrid.addView(
                textView, GridLayout.LayoutParams(
                    GridLayout.spec(row, GridLayout.CENTER),
                    GridLayout.spec(0, GridLayout.CENTER)
                )
            )
        }

        // Create column headers
        for (col in 1 until totalCols) {
            val headerText = if (col == 0) "" else (col).toString()
            val textView = createHeaderTextView(headerText)
            gameGrid.addView(
                textView, GridLayout.LayoutParams(
                    GridLayout.spec(0, GridLayout.CENTER),
                    GridLayout.spec(col, GridLayout.CENTER)
                )
            )
        }

        // Create the game buttons
        for (row in 1 until totalRows) {
            for (col in 1 until totalCols) {
                val button = Button(this).apply {
                    layoutParams = GridLayout.LayoutParams(
                        GridLayout.spec(row, 1f),
                        GridLayout.spec(col, 1f)
                    ).also {
                        it.width = 0
                        it.height = 0
                        it.setMargins(margin, margin, margin, margin)
                    }
                    //text = "$row x $col"
                    setOnClickListener { handleTileClick(this, row, col) }
                    setBackgroundColor(Color.LTGRAY)
                }
                button.tag = Pair(row, col)
                gameGrid.addView(button)
            }
        }
        suggestTile() // Suggest an initial tile to solve
    }

    private fun createHeaderTextView(text: String): TextView {
        return TextView(this).apply {
            layoutParams = GridLayout.LayoutParams(
                GridLayout.spec(GridLayout.UNDEFINED, 1f),
                GridLayout.spec(GridLayout.UNDEFINED, 1f)
            ).also {
                it.width = 0
                it.height = GridLayout.LayoutParams.WRAP_CONTENT
                it.setMargins(margin, margin, margin, margin)
            }
            gravity = Gravity.CENTER
            setText(text)
            setBackgroundColor(Color.WHITE)
        }
    }


    private fun handleTileClick(button: Button, row: Int, col: Int) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_input, null)
        val editText = dialogView.findViewById<EditText>(R.id.editTextInput)

        AlertDialog.Builder(this)
            .setTitle("What's $row x $col?")
            .setView(dialogView)
            .setPositiveButton("Submit") { dialog, _ ->
                val userAnswer = editText.text.toString().toIntOrNull()
                if (userAnswer == row * col) {
                    button.isEnabled = false
                    correctAnswersCount++
                    solvedTiles[row][col] = true
                    button.isEnabled = false
                    button.background.alpha = 64 // Value between 0 (fully transparent) and 255 (fully opaque)
                    checkGameCompletion()
                    suggestTile() // Suggest a new tile to solve
                } else {
                    button.setBackgroundColor(Color.RED)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun checkGameCompletion() {
        if (correctAnswersCount >= targetCorrectAnswers) {
            completed = true
            makeAllTilesSolved()

            // Show the completion dialog
            AlertDialog.Builder(this)
                .setTitle("Congratulations!")
                .setMessage("You have completed the game.")
                .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                .show()
        }
    }

    private fun makeAllTilesSolved() {
        for (i in 0 until gameGrid.childCount) {
            val child = gameGrid.getChildAt(i)
            if (child is Button) {
                child.isEnabled = false
                child.background.alpha = 0 // Value between 0 (fully transparent) and 255 (fully opaque)
            }
        }
    }

    private fun suggestTile() {
        if (!completed) {
            val unsolvedTiles = mutableListOf<Button>()
            for (i in 0 until gameGrid.childCount) {
                val button = gameGrid.getChildAt(i) as? Button
                val position = button?.tag as? Pair<Int, Int>
                if (button != null && position != null && !solvedTiles[position.first - 1][position.second - 1]) {
                    unsolvedTiles.add(button)
                }
            }

            if (unsolvedTiles.isNotEmpty()) {
                val randomIndex = (unsolvedTiles.indices).random()
                suggestedTile = unsolvedTiles[randomIndex]
                suggestedTile?.setBackgroundColor(Color.YELLOW) // Highlight color
            }
        }
    }
}

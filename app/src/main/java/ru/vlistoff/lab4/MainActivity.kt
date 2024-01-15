package ru.vlistoff.lab4

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.*
import androidx.lifecycle.ViewModelProvider
import ru.vlistoff.lab4.database.NoteDatabase
import ru.vlistoff.lab4.viewmodel.NoteViewModel
import ru.vlistoff.lab4.viewmodel.NoteViewModelFactory

class MainActivity : AppCompatActivity() {
    private lateinit var notesListView: ListView
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var viewModel: NoteViewModel
    private lateinit var noteDatabase: NoteDatabase
    private lateinit var addButton: Button
    private lateinit var noteEditText: EditText
    private var editMode = false
    private var editPosition = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        notesListView = findViewById(R.id.notesListView)
        //получаем инстанс БД
        noteDatabase = NoteDatabase.newInstance(applicationContext)

        val noteDao = noteDatabase.noteDao()

        //создание вьюмодели
        viewModel =
            ViewModelProvider(this, NoteViewModelFactory(noteDao))[NoteViewModel::class.java]
        viewModel.loadNotes()

        adapter = if (viewModel.notes.value != null) {
            ArrayAdapter(
                this,
                android.R.layout.simple_list_item_1,
                viewModel.notes.value!!.map { it.text })
        } else {
            ArrayAdapter(this, android.R.layout.simple_list_item_1, mutableListOf())
        }

        notesListView.adapter = adapter

        addButton = findViewById(R.id.addButton)
        noteEditText = findViewById(R.id.noteEditText)

        notesListView.setOnItemClickListener { _, _, position, _ ->
            editMode = true
            editPosition = position

            noteEditText.setText(viewModel.notes.value?.get(editPosition)?.text)
            addButton.text = "Сохранить"
        }

        addButton.setOnClickListener {
            val noteText = noteEditText.text.toString()
            if (noteText.isNotBlank()) {                //не пустой
                if (editMode) {
                    // Обновление существующей заметки
                    val existingNote = viewModel.notes.value?.get(editPosition)
                    existingNote?.text = noteText
                    if (existingNote != null) {
                        viewModel.editNote(existingNote)
                    }
                    editMode = false
                    editPosition = -1 //ничего не редактируем
                    addButton.text = "Добавить"
                    noteEditText.setText("")
                } else {
                    // Добавление новой заметки в базу данных
                    viewModel.addNote(noteText)
                    noteEditText.setText("")
                }
            }
        }

        // Обработка долгого нажатия на элемент списка (для удаления)
        notesListView.setOnItemLongClickListener { _, _, position, _ ->
            val note = viewModel.notes.value?.get(position)
            if (note != null) {
                viewModel.deleteNote(note)
            }
            adapter.notifyDataSetChanged()

            true
        }

        viewModel.notes.observe(this) { notes ->
            adapter.clear()
            adapter.addAll(notes.map { it.text })

        }
    }
}

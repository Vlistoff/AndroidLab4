package ru.vlistoff.lab4.viewmodel

import androidx.lifecycle.*
import kotlinx.coroutines.launch
import ru.vlistoff.lab4.dao.NoteDao
import ru.vlistoff.lab4.entity.Note

class NoteViewModel(private val noteDao: NoteDao) : ViewModel() {
    private val _notes = MutableLiveData<List<Note>>()

    val notes: LiveData<List<Note>>
        get() = _notes

    fun addNote(text: String) {
        viewModelScope.launch {
            val note = Note(text = text)
            noteDao.insertNote(note)
        }
        loadNotes()
    }

    fun editNote(note: Note) {
        viewModelScope.launch {
            noteDao.updateNote(note)
        }
        loadNotes()
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            noteDao.deleteNote(note)
        }
        loadNotes()
    }

    fun loadNotes() {
        viewModelScope.launch {
            val notes = noteDao.getNotes()
            _notes.value = notes
        }
    }
}




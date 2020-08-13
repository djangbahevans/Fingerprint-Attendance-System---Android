package com.example.clockin.ui.addemployee

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel

class AddEmployeeViewModel : ViewModel() {
    private var stringIndex = MutableLiveData<String>()
    val index: LiveData<Int> get() = Transformations.map(stringIndex) { it.toInt() }
    private var firstName = MutableLiveData<String>()
    val stringFirstName: LiveData<String> get() = firstName
    private var lastName = MutableLiveData<String>()
    val stringLastName: LiveData<String> get() = lastName
    private var department = MutableLiveData<String>()
    val stringDepartment: LiveData<String> get() = department
}
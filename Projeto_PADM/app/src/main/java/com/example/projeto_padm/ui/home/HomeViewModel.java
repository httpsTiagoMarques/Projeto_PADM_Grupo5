package com.example.projeto_padm.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

// Classe ViewModel responsável por armazenar e gerir os dados do fragmento Home
public class HomeViewModel extends ViewModel {

    // LiveData mutável que contém o texto a ser exibido na interface
    private final MutableLiveData<String> mText;

    // Construtor — inicializa o LiveData com um valor padrão
    public HomeViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is home fragment");
    }

    // Retorna o LiveData (somente leitura) para observação no fragmento
    public LiveData<String> getText() {
        return mText;
    }
}
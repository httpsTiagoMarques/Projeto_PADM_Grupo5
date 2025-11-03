package com.example.projeto_padm.ui.gallery;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

// Classe ViewModel responsável por gerir e armazenar dados da UI do fragmento Gallery
public class GalleryViewModel extends ViewModel {

    // LiveData mutável que contém o texto exibido no fragmento
    private final MutableLiveData<String> mText;

    // Construtor — inicializa o LiveData com um valor padrão
    public GalleryViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is gallery fragment");
    }

    // Retorna o LiveData (imutável) para observação no fragmento
    public LiveData<String> getText() {
        return mText;
    }
}
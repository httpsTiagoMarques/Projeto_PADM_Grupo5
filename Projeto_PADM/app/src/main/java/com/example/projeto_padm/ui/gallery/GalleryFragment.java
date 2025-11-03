package com.example.projeto_padm.ui.gallery;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.projeto_padm.Ambiente;
import com.example.projeto_padm.AppDatabase;
import com.example.projeto_padm.Categoria;
import com.example.projeto_padm.Percurso;
import com.example.projeto_padm.R;
import com.example.projeto_padm.SyncManager;
import com.example.projeto_padm.TipoTreino;
import com.example.projeto_padm.Treino;
import com.example.projeto_padm.User;
import com.example.projeto_padm.databinding.FragmentGalleryBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class GalleryFragment extends Fragment {


    // Declarações
    private FragmentGalleryBinding binding;
    private int selectedCategoriaIndex = -1;

    private DatabaseReference treinoRef;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Location ultimaLocalizacao = null;

    private long inicioTreinoMillis = 0;
    private long tempoPausadoTotal = 0; // total de tempo pausado
    private long inicioPausaMillis = 0;  // momento em que a pausa começou
    private boolean emPausa = false;
    private double totalDistancia = 0.0;

    private static final int REQUEST_LOCATION_PERMISSION = 1001;
    private AppDatabase db;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentGalleryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        treinoRef = FirebaseDatabase.getInstance().getReference("Treinos"); // Firebase
        db = AppDatabase.getInstance(requireContext()); // BD local

        Spinner spinnerAmbiente = binding.spinnerAmbiente;
        Spinner spinnerTipoTreino = binding.spinnerTipoTreino;
        Spinner spinnerPercurso = binding.spinnerPercurso;
        TextView labelPercurso = binding.labelPercurso;

        Button btnIniciar = binding.btnIniciar;
        Button btnPausar = binding.btnPausar;
        Button btnTerminar = binding.btnTerminar;

        TextView textCategoria1 = binding.textCategoria1;
        TextView textCategoria2 = binding.textCategoria2;
        TextView textCategoria3 = binding.textCategoria3;

        CardView card1 = binding.cardCategoria1;
        CardView card2 = binding.cardCategoria2;
        CardView card3 = binding.cardCategoria3;

        spinnerPercurso.setVisibility(View.GONE);
        labelPercurso.setVisibility(View.GONE);
        btnPausar.setVisibility(View.GONE);
        btnTerminar.setVisibility(View.GONE);

        // Solicita permissão de localização
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            prepopulateIfNeeded(db); // Preenche BD se vazio
            List<Categoria> categorias = db.categoriaDao().getAllCategories();
            List<Ambiente> ambientes = db.ambienteDao().getAllAmbiente();
            List<TipoTreino> tipos = db.tipoTreinoDao().getAllTipoTreino();
            List<Percurso> percursos = db.percursoDao().getAllPercurso();

            // Cria arrays para os spinners
            String[] nomesAmbiente = new String[ambientes.size()];
            for (int i = 0; i < ambientes.size(); i++)
                nomesAmbiente[i] = ambientes.get(i).getNome();

            String[] nomesTipo = new String[tipos.size()];
            for (int i = 0; i < tipos.size(); i++)
                nomesTipo[i] = tipos.get(i).getNome();

            String[] nomesPercurso = new String[percursos.size()];
            for (int i = 0; i < percursos.size(); i++)
                nomesPercurso[i] = percursos.get(i).getNome();

            requireActivity().runOnUiThread(() -> {
                // Mostra categorias
                if (categorias.size() >= 3) {
                    textCategoria1.setText(categorias.get(0).getNome());
                    textCategoria2.setText(categorias.get(1).getNome());
                    textCategoria3.setText(categorias.get(2).getNome());
                }

                // Adapta spinners
                ArrayAdapter<String> adapterAmbiente = new ArrayAdapter<>(requireContext(),
                        android.R.layout.simple_spinner_item, nomesAmbiente);
                spinnerAmbiente.setAdapter(adapterAmbiente);

                ArrayAdapter<String> adapterTipo = new ArrayAdapter<>(requireContext(),
                        android.R.layout.simple_spinner_item, nomesTipo);
                spinnerTipoTreino.setAdapter(adapterTipo);

                ArrayAdapter<String> adapterPercurso = new ArrayAdapter<>(requireContext(),
                        android.R.layout.simple_spinner_item, nomesPercurso);
                spinnerPercurso.setAdapter(adapterPercurso);

                // Mostra percurso apenas se tipo = Programado
                spinnerTipoTreino.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        boolean programado = nomesTipo[position].equalsIgnoreCase("Programado");
                        spinnerPercurso.setVisibility(programado ? View.VISIBLE : View.GONE);
                        labelPercurso.setVisibility(programado ? View.VISIBLE : View.GONE);
                    }

                    @Override public void onNothingSelected(AdapterView<?> parent) {}
                });

                // Click nas categorias
                View.OnClickListener clickCategoria = v -> {
                    card1.setCardBackgroundColor(0xFFFFFFFF);
                    card2.setCardBackgroundColor(0xFFFFFFFF);
                    card3.setCardBackgroundColor(0xFFFFFFFF);
                    ((CardView) v).setCardBackgroundColor(0xFFE0E0E0);
                    if (v == card1) selectedCategoriaIndex = 0;
                    else if (v == card2) selectedCategoriaIndex = 1;
                    else if (v == card3) selectedCategoriaIndex = 2;
                };
                card1.setOnClickListener(clickCategoria);
                card2.setOnClickListener(clickCategoria);
                card3.setOnClickListener(clickCategoria);

                // =====================================================
                // === BOTÃO INICIAR TREINO ===
                // =====================================================
                btnIniciar.setOnClickListener(v -> {
                    // Sem categoria selecionada
                    if (selectedCategoriaIndex == -1) {
                        Toast.makeText(requireContext(), "Selecione uma categoria primeiro!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Userid guardado em sharedPreferences
                    SharedPreferences prefs = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                    long userId = prefs.getLong("userId", -1);
                    if (userId == -1) {
                        Toast.makeText(requireContext(), "Erro: utilizador não encontrado!", Toast.LENGTH_LONG).show();
                        return;
                    }

                    // Online?
                    if (!isConnected()) {
                        Toast.makeText(requireContext(),
                                "Para iniciar um novo treino tem de estar online.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    btnIniciar.setEnabled(false);

                    // Obtém seleções
                    int ambienteIndex = spinnerAmbiente.getSelectedItemPosition();
                    int tipoIndex = spinnerTipoTreino.getSelectedItemPosition();
                    int percursoIndex = spinnerPercurso.getSelectedItemPosition();

                    long ambienteId = ambientes.get(ambienteIndex).getId();
                    long categoriaId = categorias.get(selectedCategoriaIndex).getId();
                    long tipoTreinoId = tipos.get(tipoIndex).getId();
                    Long percursoId = (spinnerPercurso.getVisibility() == View.VISIBLE)
                            ? percursos.get(percursoIndex).getId() : null;

                    // Data e hora atuais
                    String data = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                    String horaInicio = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());

                    // Cria novo treino
                    Treino novoTreino = new Treino();
                    novoTreino.setCategoriaId(categoriaId);
                    novoTreino.setAmbienteId(ambienteId);
                    novoTreino.setTipoTreinoId(tipoTreinoId);
                    novoTreino.setPercursoId(percursoId);
                    novoTreino.setUserId(userId);
                    novoTreino.setData(data);
                    novoTreino.setHora_inicio(horaInicio);

                    // Gera novo ID no Firebase
                    treinoRef.orderByKey().limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            long nextId = 1;
                            for (DataSnapshot child : snapshot.getChildren()) {
                                try {
                                    long lastId = Long.parseLong(child.getKey());
                                    nextId = lastId + 1;
                                } catch (Exception ignored) {}
                            }

                            long finalNextId = nextId;
                            novoTreino.setId(finalNextId);

                            Executors.newSingleThreadExecutor().execute(() -> {
                                try {
                                    // Insere localmente
                                    db.treinoDao().insert(novoTreino);
                                    prefs.edit().putLong("treinoAtualId", finalNextId).apply();

                                    // Envia Firebase
                                    treinoRef.child(String.valueOf(finalNextId)).setValue(novoTreino);

                                    // Sincroniza tudo
                                    new SyncManager(requireContext()).syncAll(userId);

                                    //Inicia contagem
                                    inicioTreinoMillis = System.currentTimeMillis();
                                    totalDistancia = 0;
                                    ultimaLocalizacao = null;
                                    emPausa = false;

                                    requireActivity().runOnUiThread(() -> {
                                        Toast.makeText(requireContext(), "Treino iniciado!", Toast.LENGTH_SHORT).show();
                                        vibrar(500); // vibra ao iniciar
                                        btnIniciar.setVisibility(View.GONE);
                                        btnPausar.setVisibility(View.VISIBLE);
                                        btnTerminar.setVisibility(View.VISIBLE);
                                        btnPausar.setText("Pausar");
                                        // Inicia o tracking do GPS
                                        startLocationTracking();
                                    });

                                } catch (Exception e) {
                                    Log.e("ERROR_TREINO_INSERT", "Erro ao inserir treino: " + e.getMessage());
                                }
                            });
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(requireContext(), "Erro ao verificar ID no Firebase", Toast.LENGTH_SHORT).show();
                        }
                    });
                });

                // =====================================================
                // === BOTÃO PAUSAR TREINO ===
                // =====================================================
                btnPausar.setOnClickListener(v -> {
                    emPausa = !emPausa;
                    if (emPausa) {
                        vibrar(700);
                        btnPausar.setText("Retomar");
                        Toast.makeText(requireContext(), "Treino em pausa", Toast.LENGTH_SHORT).show();

                        // Guarda o momento em que a pausa começou
                        inicioPausaMillis = System.currentTimeMillis();

                    } else {
                        vibrar(700);
                        btnPausar.setText("Pausar");
                        Toast.makeText(requireContext(), "Treino retomado", Toast.LENGTH_SHORT).show();

                        // Soma o tempo de pausa total
                        tempoPausadoTotal += System.currentTimeMillis() - inicioPausaMillis;
                    }
                });



                // =====================================================
                // === BOTÃO TERMINAR TREINO ===
                // =====================================================
                btnTerminar.setOnClickListener(v -> {
                    SharedPreferences prefs = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                    long treinoId = prefs.getLong("treinoAtualId", -1);
                    if (treinoId == -1) {
                        Toast.makeText(requireContext(), "Nenhum treino ativo!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (locationManager != null)
                        locationManager.removeUpdates(locationListener);

                    // Cálculo de tempo e distância
                    long duracaoMillis = System.currentTimeMillis() - inicioTreinoMillis - tempoPausadoTotal;
                    double km = totalDistancia / 1000.0;
                    double horas = duracaoMillis / 3600000.0;
                    double velocidadeMedia = (horas > 0) ? km / horas : 0;
                    double duracaoMin = duracaoMillis / 60000.0;

                    // MET conforme categoria
                    double met;
                    if (selectedCategoriaIndex == 0)      met = 3.5;
                    else if (selectedCategoriaIndex == 1) met = 8.0;
                    else if (selectedCategoriaIndex == 2) met = 6.8;
                    else met = 5.0;

                    Executors.newSingleThreadExecutor().execute(() -> {
                        try {
                            long userId = prefs.getLong("userId", -1);
                            User user = db.userDao().getUserById(userId);
                            float pesoKg = (user != null) ? user.getPeso() : 70;

                            int calorias = (int) (duracaoMin * (met * 3.5 * pesoKg) / 200);

                            final double finalKm = Math.round(km * 100.0) / 100.0;
                            final double finalVelocidade = Math.round(velocidadeMedia * 100.0) / 100.0;

                            String duracaoFormatada = String.format(Locale.getDefault(), "%02d:%02d",
                                    (duracaoMillis / 60000), (duracaoMillis / 1000) % 60);
                            String horaFim = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());

                            // Atualiza treino
                            Treino treino = db.treinoDao().getTreinoById(treinoId);
                            treino.setTempo(duracaoFormatada);
                            treino.setDistanciaPercorrida(finalKm);
                            treino.setVelocidadeMedia(finalVelocidade);
                            treino.setCalorias(calorias);
                            treino.setHora_fim(horaFim);

                            db.treinoDao().update(treino);
                            treinoRef.child(String.valueOf(treinoId)).setValue(treino);

                            prefs.edit().remove("treinoAtualId").apply();

                            requireActivity().runOnUiThread(() -> {
                                vibrar(700); // Vibra ao terminar
                                Toast.makeText(requireContext(),
                                        "Treino concluído!\n" +
                                                "Duração: " + duracaoFormatada +
                                                "\nDistância: " + String.format(Locale.getDefault(), "%.2f km", finalKm) +
                                                "\nVelocidade média: " + String.format(Locale.getDefault(), "%.2f km/h", finalVelocidade) +
                                                "\nCalorias: " + calorias + " kcal",
                                        Toast.LENGTH_LONG).show();

                                NavHostFragment.findNavController(GalleryFragment.this)
                                        .navigate(R.id.nav_home); // Volta à home
                            });

                        } catch (Exception e) {
                            Log.e("ERROR_TREINO_UPDATE", "Erro ao atualizar treino: " + e.getMessage());
                        }
                    });
                });
            });
        });

        return root;
    }

    // Inicia o tracking GPS
    private void startLocationTracking() {
        locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);
        locationListener = location -> {
            if (!emPausa) {
                if (ultimaLocalizacao != null) {
                    float distancia = location.distanceTo(ultimaLocalizacao);
                    totalDistancia += distancia;
                }
                ultimaLocalizacao = location;
            }
        };

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 1, locationListener);
        }
    }

    // Preenche base de dados local se vazia
    private void prepopulateIfNeeded(AppDatabase db) {
        if (db.categoriaDao().getAllCategories().isEmpty()) {
            db.categoriaDao().insert(new Categoria("Caminhada", "Exercício leve"));
            db.categoriaDao().insert(new Categoria("Corrida", "Corrida a pé"));
            db.categoriaDao().insert(new Categoria("Ciclismo", "Exercício com bicicleta"));
        }

        if (db.ambienteDao().getAllAmbiente().isEmpty()) {
            db.ambienteDao().insert(new Ambiente("Interior", "Ambiente interno"));
            db.ambienteDao().insert(new Ambiente("Exterior", "Ambiente externo"));
        }

        if (db.tipoTreinoDao().getAllTipoTreino().isEmpty()) {
            db.tipoTreinoDao().insert(new TipoTreino("Livre", "Treino livre"));
            db.tipoTreinoDao().insert(new TipoTreino("Programado", "Treino por percurso"));
        }

        if (db.percursoDao().getAllPercurso().isEmpty()) {
            db.percursoDao().insert(new Percurso("Percurso Curto", "3 km", 3.0f));
            db.percursoDao().insert(new Percurso("Percurso Longo", "10 km", 10.0f));
        }
    }

    // Vibração
    private void vibrar(long duracaoMs) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            VibratorManager vm = (VibratorManager) requireContext().getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
            Vibrator vibrator = vm.getDefaultVibrator();
            vibrator.vibrate(VibrationEffect.createOneShot(duracaoMs, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            Vibrator vibrator = (Vibrator) requireContext().getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(VibrationEffect.createOneShot(duracaoMs, VibrationEffect.DEFAULT_AMPLITUDE));
        }
    }

    // Verifica conexão à internet
    private boolean isConnected() {
        ConnectivityManager cm =
                (ConnectivityManager) requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (locationManager != null && locationListener != null)
            locationManager.removeUpdates(locationListener); // Para GPS
        binding = null;
    }
}

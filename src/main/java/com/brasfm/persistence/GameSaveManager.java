package com.brasfm.persistence;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Gerenciador de salvamento/carregamento do jogo.
 * Usa Gson para serialização JSON.
 */
public class GameSaveManager {

    private static final String SAVES_DIRECTORY = "saves";
    private static final String FILE_EXTENSION = ".json";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    private final Gson gson;
    private final Path savesPath;

    public GameSaveManager() {
        // Configura Gson com formatação legível e adaptadores customizados
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .serializeNulls()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();

        // Cria diretório de saves se não existir
        this.savesPath = Paths.get(SAVES_DIRECTORY);
        try {
            Files.createDirectories(savesPath);
        } catch (IOException e) {
            System.err.println("Erro ao criar diretório de saves: " + e.getMessage());
        }
    }

    /**
     * Salva o jogo em um arquivo JSON.
     */
    public boolean save(SaveGame saveGame, String nomeArquivo) {
        if (saveGame == null || nomeArquivo == null || nomeArquivo.trim().isEmpty()) {
            return false;
        }

        // Limpa nome do arquivo
        String nomeSeguro = nomeArquivo.replaceAll("[^a-zA-Z0-9._-]", "_");
        if (!nomeSeguro.endsWith(FILE_EXTENSION)) {
            nomeSeguro += FILE_EXTENSION;
        }

        saveGame.setNomeArquivo(nomeSeguro);
        saveGame.setDataSave(LocalDateTime.now());
        saveGame.atualizarPreview();

        Path arquivo = savesPath.resolve(nomeSeguro);

        try (Writer writer = Files.newBufferedWriter(arquivo, StandardCharsets.UTF_8)) {
            gson.toJson(saveGame, writer);
            System.out.println("✅ Jogo salvo em: " + arquivo.toAbsolutePath());
            return true;
        } catch (IOException e) {
            System.err.println("❌ Erro ao salvar jogo: " + e.getMessage());
            return false;
        }
    }

    /**
     * Salva com nome automático baseado no time e data.
     */
    public boolean save(SaveGame saveGame) {
        String timeNome = saveGame.getTimeJogador() != null
                ? saveGame.getTimeJogador().getSigla()
                : "save";
        String timestamp = LocalDateTime.now().format(DATE_FORMAT);
        String nomeArquivo = timeNome + "_" + timestamp;
        return save(saveGame, nomeArquivo);
    }

    /**
     * Carrega um jogo de um arquivo JSON.
     */
    public SaveGame load(String nomeArquivo) {
        if (nomeArquivo == null || nomeArquivo.trim().isEmpty()) {
            return null;
        }

        if (!nomeArquivo.endsWith(FILE_EXTENSION)) {
            nomeArquivo += FILE_EXTENSION;
        }

        Path arquivo = savesPath.resolve(nomeArquivo);

        if (!Files.exists(arquivo)) {
            System.err.println("❌ Arquivo não encontrado: " + arquivo);
            return null;
        }

        try (Reader reader = Files.newBufferedReader(arquivo, StandardCharsets.UTF_8)) {
            SaveGame saveGame = gson.fromJson(reader, SaveGame.class);
            System.out.println("✅ Jogo carregado: " + nomeArquivo);
            return saveGame;
        } catch (Exception e) {
            System.err.println("❌ Erro ao carregar jogo: " + e.getMessage());
            return null;
        }
    }

    /**
     * Carrega de um File diretamente.
     */
    public SaveGame load(File file) {
        if (file == null || !file.exists()) {
            return null;
        }

        try (Reader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            SaveGame saveGame = gson.fromJson(reader, SaveGame.class);
            System.out.println("✅ Jogo carregado: " + file.getName());
            return saveGame;
        } catch (Exception e) {
            System.err.println("❌ Erro ao carregar jogo: " + e.getMessage());
            return null;
        }
    }

    /**
     * Lista todos os saves disponíveis.
     */
    public List<SaveGame> listarSaves() {
        List<SaveGame> saves = new ArrayList<>();

        if (!Files.exists(savesPath)) {
            return saves;
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(savesPath, "*" + FILE_EXTENSION)) {
            for (Path arquivo : stream) {
                try {
                    SaveGame save = load(arquivo.getFileName().toString());
                    if (save != null) {
                        saves.add(save);
                    }
                } catch (Exception e) {
                    System.err.println("Erro ao ler save: " + arquivo.getFileName());
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao listar saves: " + e.getMessage());
        }

        // Ordena por data (mais recente primeiro)
        saves.sort((a, b) -> {
            if (a.getDataSave() == null)
                return 1;
            if (b.getDataSave() == null)
                return -1;
            return b.getDataSave().compareTo(a.getDataSave());
        });

        return saves;
    }

    /**
     * Deleta um save.
     */
    public boolean deletar(String nomeArquivo) {
        if (!nomeArquivo.endsWith(FILE_EXTENSION)) {
            nomeArquivo += FILE_EXTENSION;
        }

        Path arquivo = savesPath.resolve(nomeArquivo);

        try {
            return Files.deleteIfExists(arquivo);
        } catch (IOException e) {
            System.err.println("Erro ao deletar save: " + e.getMessage());
            return false;
        }
    }

    /**
     * Retorna o caminho do diretório de saves.
     */
    public Path getSavesPath() {
        return savesPath;
    }

    /**
     * Adapter para serialização de LocalDateTime.
     */
    private static class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {
        private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        @Override
        public void write(JsonWriter out, LocalDateTime value) throws IOException {
            if (value == null) {
                out.nullValue();
            } else {
                out.value(FORMATTER.format(value));
            }
        }

        @Override
        public LocalDateTime read(JsonReader in) throws IOException {
            String value = in.nextString();
            if (value == null || value.isEmpty()) {
                return null;
            }
            return LocalDateTime.parse(value, FORMATTER);
        }
    }
}

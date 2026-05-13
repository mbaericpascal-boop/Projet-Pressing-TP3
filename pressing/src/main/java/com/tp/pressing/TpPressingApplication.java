package com.tp.pressing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.io.File;

@SpringBootApplication
public class TpPressingApplication {

    public static void main(String[] args) {
        // Créer le dossier de la base de données s'il n'existe pas
        File dbDir = new File(System.getProperty("user.home") + "/pressing-data");
        if (!dbDir.exists()) {
            dbDir.mkdirs();
            System.out.println("✅ Dossier BD créé : " + dbDir.getAbsolutePath());
        }

        SpringApplication.run(TpPressingApplication.class, args);
    }
}
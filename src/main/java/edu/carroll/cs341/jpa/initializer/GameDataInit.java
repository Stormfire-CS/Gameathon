package edu.carroll.cs341.jpa.initializer;

import edu.carroll.cs341.jpa.model.Games;
import edu.carroll.cs341.jpa.repo.GamesRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Scanner;

@Component
public class GameDataInit implements CommandLineRunner {

    private final GamesRepository gamesRepo;

    public GameDataInit(GamesRepository gamesRepo) {
        this.gamesRepo = gamesRepo;
    }

    @Override
    public void run(String... args) {
        try {
            Scanner fileReader = new Scanner(new File("src/main/resources/data/games.txt"));

            while (fileReader.hasNextLine()) {
                String line = fileReader.nextLine();

                String[] data = line.split("\\|");

                Games game = new Games(data[0]);
                if (!gamesRepo.existsByGameName(data[0])) {
                    game.setMinPlayers(Integer.parseInt(data[1]));
                    game.setMaxPlayers(Integer.parseInt(data[2]));
                    gamesRepo.save(game);
                }
            }
            fileReader.close();
        } catch (Exception e) {
            System.out.println("Could not load games.txt: " + e.getMessage());
        }
    }
}

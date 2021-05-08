package com.game.service;

import com.game.controller.PlayerOrder;
import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;

import java.util.Date;
import java.util.List;

public interface PlayerService {
    List<Player> getPlayers(
            String name,
            String title,
            Race race,
            Profession profession,
            Long after,
            Long before,
            Boolean banned,
            Integer minExperience,
            Integer maxExperience,
            Integer minLevel,
            Integer maxLevel
    );

    List<Player> sortPLayers(List<Player> players, PlayerOrder order);

    List<Player> getPage(List<Player> players,Integer pageNumber,Integer pageSize);

    Player getPlayer(Long id);

    Player savePlayer(Player player);

    Boolean isPlayerValid(Player player);

    Player updatePLayer(Player oldPlayer,Player newPlayer) throws IllegalArgumentException;

    void deletePlayer(Player player);

    Integer ComputeLevel(Integer experience);

    Integer ComputeUntilNextLevel(Integer level,Integer experience);

    boolean isExperienceValid(Integer experience);

}


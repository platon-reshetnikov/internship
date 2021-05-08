package com.game.service;

import com.game.controller.PlayerOrder;
import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class PlayerServiceImpl implements PlayerService {
    private PlayerRepository playerRepository;

    public PlayerServiceImpl() {
    }

    @Autowired
    public PlayerServiceImpl(PlayerRepository playerRepository) {
        super();
        this.playerRepository = playerRepository;
    }


    @Override
    public List<Player> getPlayers(String name,
                                   String title,
                                   Race race,
                                   Profession profession,
                                   Long after,
                                   Long before,
                                   Boolean banned,
                                   Integer minExperience,
                                   Integer maxExperience,
                                   Integer minLevel,
                                   Integer maxLevel) {

        final Date afterDate = after == null ? null : new Date(after);
        final Date beforeDate = before == null ? null : new Date(before);
        final List<Player> list = new ArrayList<>();
        playerRepository.findAll().forEach((player) -> {
            if (name != null && !player.getName().contains(name)) return;
            if (title != null && !player.getTitle().contains(title)) return;
            if (race != null && player.getRace() != race) return;
            if (profession != null && player.getProfession() != profession) return;
            if (afterDate != null && player.getBirthday().before(afterDate)) return;
            if (beforeDate != null && player.getBirthday().after(beforeDate)) return;
            if (banned != null && player.getBanned().booleanValue() != banned.booleanValue()) return;
            if (minExperience != null && player.getExperience().compareTo(minExperience) < 0) return;
            if (maxExperience != null && player.getExperience().compareTo(maxExperience) > 0) return;
            if (minLevel != null && player.getLevel().compareTo(minLevel) < 0) return;
            if (maxLevel != null && player.getLevel().compareTo(maxLevel) > 0) return;
            list.add(player);
        });
        return list;
    }


    @Override
    public List<Player> getPage(List<Player> players, Integer pageNumber, Integer pageSize) {
        final Integer page = pageNumber == null ? 0 : pageNumber;
        final Integer size = pageSize == null ? 3 : pageSize;
        final int from = page * size;
        int to = from + size;
        if (to > players.size()) to = players.size();
        return players.subList(from, to);
    }

    @Override
    public Player getPlayer(Long id) {
        return playerRepository.findById(id).orElse(null);
    }

    @Override
    public Player savePlayer(Player player) {
        return playerRepository.save(player);
    }

    @Override
    public Boolean isPlayerValid(Player player) {
        return player != null && isStringValidName(player.getName()) && isStringValidTitle(player.getTitle())
                    && isBirthDayValid(player.getBirthday())
                    && isExperienceValid(player.getExperience());
    }

    @Override
    public Player updatePLayer(Player oldPlayer, Player newPlayer) throws IllegalArgumentException {
        boolean shouldLevel = false;
        boolean shouldUntilNextLevel = false;

        final String name = newPlayer.getName();
        if (name != null) {
            if (isStringValidName(name)) {
                oldPlayer.setName(name);
            } else {
                throw new IllegalArgumentException();
            }
        }

        final String title = newPlayer.getTitle();
        if (title != null) {
            if (isStringValidTitle(title)) {
                oldPlayer.setTitle(title);
            } else {
                throw new IllegalArgumentException();
            }
        }

        if (newPlayer.getRace() != null) {
            oldPlayer.setRace(newPlayer.getRace());
        }

        if (newPlayer.getProfession() != null){
            oldPlayer.setProfession(newPlayer.getProfession());
        }

        final Date birthday = newPlayer.getBirthday();
        if (birthday != null) {
            if (isBirthDayValid(birthday)) {
                oldPlayer.setBirthday(birthday);
            } else {
                throw new IllegalArgumentException();
            }
        }

        if (newPlayer.getBanned() != null) {
            oldPlayer.setBanned(newPlayer.getBanned());
        }

        final Integer experience = newPlayer.getExperience();
        if (experience != null) {
            if (isExperienceValid(experience)) {
                oldPlayer.setExperience(experience);
                shouldLevel = true;
            } else {
                throw new IllegalArgumentException();
            }
        }

        if (shouldLevel) {
            final Integer level = ComputeLevel(experience);

            if (level != null) {
                if (isLevelValid(level)) {
                    oldPlayer.setLevel(level);
                    shouldUntilNextLevel = true;
                } else {
                    throw new IllegalArgumentException();
                }
            }
        }
        if (shouldUntilNextLevel) {
            final Integer untilNextLevel = ComputeUntilNextLevel(oldPlayer.getLevel(), experience);

            if (untilNextLevel != null) {
                if (isUntilNextLevelValid(untilNextLevel)) {
                    oldPlayer.setUntilNextLevel(untilNextLevel);
                } else {
                    throw new IllegalArgumentException();
                }
            }
        }
        playerRepository.save(oldPlayer);
        return oldPlayer;
    }

    @Override
    public void deletePlayer(Player player) {
        playerRepository.delete(player);
    }

    @Override
    public List<Player> sortPLayers(List<Player> players, PlayerOrder order) {
        if (order != null) {
            players.sort((player1, player2) -> {
                switch (order) {
                    case ID: return player1.getId().compareTo(player2.getId());
                    case NAME: return player1.getName().compareTo(player2.getName());
                    case EXPERIENCE: return player1.getExperience().compareTo(player2.getExperience());
                    case BIRTHDAY: return player1.getBirthday().compareTo(player2.getBirthday());
                    default: return 0;
                }
            });
        }
        return players;
    }

    private boolean isStringValidName(String value) {
        final int maxStringLength = 12;
        return value != null && !value.isEmpty() && value.length() <= maxStringLength;
    }

    private boolean isStringValidTitle(String value) {
        final int maxStringLength = 30;
        return value != null && !value.isEmpty() && value.length() <= maxStringLength;
    }

    @Override
    public boolean isExperienceValid(Integer experience) {
        final int minExperience = 0;
        final int maxExperience = 10000000;
        return experience != null && experience.compareTo(minExperience) >= 0 && experience.compareTo(maxExperience) <= 0;
    }

    private boolean isUntilNextLevelValid(Integer untilNextLevel) {
        final int minUntilNextLevel = 0;
        final int maxUntilNextLevel = 10000000;
        return untilNextLevel != null && untilNextLevel.compareTo(minUntilNextLevel) >= 0 && untilNextLevel.compareTo(maxUntilNextLevel) <= 0;
    }

    private boolean isBirthDayValid(Date birthday) {
         final Date startProd = getDateForYear(2000);
         final Date endProd = getDateForYear(3000);
         return birthday != null && birthday.after(startProd) && birthday.before(endProd);
    }

    private Date getDateForYear(int year) {
        final Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        return calendar.getTime();
    }

    private boolean isLevelValid(Integer level) {
        final int minLevel = 1;
        final int maxLevel = 999;
        return level != null && level.compareTo(minLevel) >= 0 && level.compareTo(maxLevel) <= 0;
    }

    @Override
    public Integer ComputeLevel(Integer experience){
        return (int) (Math.sqrt(2500 + 200 * experience) - 50) / 100;
    }
    @Override
    public Integer ComputeUntilNextLevel(Integer level,Integer experience){
        return 50 * (level + 1) * (level + 2) - experience;
    }
}
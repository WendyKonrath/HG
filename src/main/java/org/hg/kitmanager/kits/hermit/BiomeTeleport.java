package org.hg.kitmanager.kits.hermit;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.hg.Main;
import org.hg.utils.GetSettings;

import java.util.*;

/**
 * Sistema de teleporte para biomas aleatórios dentro da borda do mundo
 */
public class BiomeTeleport {

    private static final Random random = new Random();

    // Lista de biomas interessantes para teleportar (exclui oceanos e rios)
    private static final Set<Biome> VALID_BIOMES = new HashSet<>(Arrays.asList(
            Biome.SWAMPLAND,
            Biome.JUNGLE,
            Biome.MUSHROOM_ISLAND
    ));

    /**
     * Teleporta o jogador para um bioma aleatório próximo dentro da borda
     */
    public static void teleportToRandomBiome(Player p) {
        World world = p.getWorld();
        int borderLimit = GetSettings.config.getConfig().getInt("Configuration.borderlimit", 1000);

        // Mensagem inicial
        p.sendMessage("§eProcurando um bioma adequado...");

        // Executa de forma assíncrona para não travar o servidor
        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> {
            Location biomeLocation = findNearestValidBiome(p.getLocation(), borderLimit);

            if (biomeLocation == null) {
                // Fallback: teleporta para local aleatório dentro da borda
                biomeLocation = getRandomLocationInBorder(world, borderLimit);
            }

            // Teleporta de forma síncrona (API do Bukkit exige)
            Location finalLocation = biomeLocation;
            Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
                p.teleport(finalLocation);

                String biomeName = getBiomeName(finalLocation.getBlock().getBiome());
                p.sendMessage("§aVocê foi teleportado para: §f" + biomeName);
            });
        });
    }

    /**
     * Encontra o bioma válido mais próximo da localização atual
     */
    private static Location findNearestValidBiome(Location start, int borderLimit) {
        World world = start.getWorld();
        int startX = start.getBlockX();
        int startZ = start.getBlockZ();

        int maxRadius = borderLimit / 2;
        int searchRadius = 16; // Blocos de chunk (16x16)

        // Busca em espiral ao redor do jogador
        for (int radius = searchRadius; radius <= maxRadius; radius += searchRadius) {
            List<Location> candidates = new ArrayList<>();

            // Verifica em um círculo ao redor
            for (int angle = 0; angle < 360; angle += 45) {
                double radians = Math.toRadians(angle);

                int x = startX + (int) (radius * Math.cos(radians));
                int z = startZ + (int) (radius * Math.sin(radians));

                // Verifica se está dentro da borda
                if (Math.abs(x) > maxRadius || Math.abs(z) > maxRadius) {
                    continue;
                }

                Biome biome = world.getBiome(x, z);

                loadChunkSmooth(world, x, z);

                if (VALID_BIOMES.contains(biome)) {
                    int y = world.getHighestBlockYAt(x, z) + 1;
                    candidates.add(new Location(world, x + 0.5, y, z + 0.5));
                }
            }

            // Se encontrou candidatos, retorna um aleatório
            if (!candidates.isEmpty()) {
                return candidates.get(random.nextInt(candidates.size()));
            }
        }

        return null;
    }

    public static void loadChunkSmooth(World world, int x, int z) {
        Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
            Location loc = new Location(world, x, 120, z);
            world.getChunkAt(loc).load(true);
        });
    }

    /**
     * Obtém uma localização completamente aleatória dentro da borda (fallback)
     */
    private static Location getRandomLocationInBorder(World world, int borderLimit) {
        int halfBorder = borderLimit / 2;

        // Gera coordenadas aleatórias dentro da borda
        int x = random.nextInt(borderLimit) - halfBorder;
        int z = random.nextInt(borderLimit) - halfBorder;

        // Obtém a altura mais alta nessa posição
        int y = world.getHighestBlockYAt(x, z) + 1;

        return new Location(world, x + 0.5, y, z + 0.5);
    }

    /**
     * Teleporta para um bioma específico (se você quiser escolher o bioma)
     */
    public static void teleportToSpecificBiome(Player p, Biome targetBiome) {
        World world = p.getWorld();
        int borderLimit = GetSettings.config.getConfig().getInt("Configuration.borderlimit", 1000);

        p.sendMessage("§eProcurando o bioma §f" + getBiomeName(targetBiome) + "§e...");

        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> {
            Location biomeLocation = findSpecificBiome(p.getLocation(), targetBiome, borderLimit);

            if (biomeLocation == null) {
                Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
                    p.sendMessage("§cNão foi possível encontrar o bioma §f" + getBiomeName(targetBiome));
                });
                return;
            }

            Location finalLocation = biomeLocation;
            Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
                p.teleport(finalLocation);
                p.sendMessage("§aVocê foi teleportado para: §f" + getBiomeName(targetBiome));
            });
        });
    }

    /**
     * Procura por um bioma específico
     */
    private static Location findSpecificBiome(Location start, Biome targetBiome, int borderLimit) {
        World world = start.getWorld();
        int startX = start.getBlockX();
        int startZ = start.getBlockZ();

        int maxRadius = borderLimit / 2;
        int searchRadius = 32;

        // Busca em espiral
        for (int radius = searchRadius; radius <= maxRadius; radius += searchRadius) {
            for (int angle = 0; angle < 360; angle += 30) {
                double radians = Math.toRadians(angle);

                int x = startX + (int) (radius * Math.cos(radians));
                int z = startZ + (int) (radius * Math.sin(radians));

                if (Math.abs(x) > maxRadius || Math.abs(z) > maxRadius) {
                    continue;
                }

                Biome biome = world.getBiome(x, z);

                if (biome == targetBiome) {
                    int y = world.getHighestBlockYAt(x, z) + 1;
                    return new Location(world, x + 0.5, y, z + 0.5);
                }
            }
        }

        return null;
    }

    /**
     * Teleporta múltiplos jogadores para biomas diferentes (útil para scatter inicial)
     */
    public static void scatterPlayersInBiomes(List<Player> players, World world) {
        int borderLimit = GetSettings.config.getConfig().getInt("Configuration.borderlimit", 1000);
        int halfBorder = borderLimit / 2;

        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> {
            Map<Player, Location> teleports = new HashMap<>();

            for (Player p : players) {
                // Tenta encontrar um bioma diferente para cada jogador
                Location location = null;
                int attempts = 0;

                while (location == null && attempts < 10) {
                    int x = random.nextInt(borderLimit) - halfBorder;
                    int z = random.nextInt(borderLimit) - halfBorder;

                    Biome biome = world.getBiome(x, z);

                    if (VALID_BIOMES.contains(biome)) {
                        int y = world.getHighestBlockYAt(x, z) + 1;
                        location = new Location(world, x + 0.5, y, z + 0.5);

                        // Verifica se não está muito perto de outro jogador
                        boolean tooClose = false;
                        for (Location other : teleports.values()) {
                            if (location.distance(other) < 100) {
                                tooClose = true;
                                break;
                            }
                        }

                        if (tooClose) {
                            location = null;
                        }
                    }

                    attempts++;
                }

                if (location == null) {
                    location = getRandomLocationInBorder(world, borderLimit);
                }

                teleports.put(p, location);
            }

            // Teleporta todos de forma síncrona
            Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
                for (Map.Entry<Player, Location> entry : teleports.entrySet()) {
                    entry.getKey().teleport(entry.getValue());
                    entry.getKey().sendMessage("§aVocê foi espalhado pelo mapa!");
                }
            });
        });
    }

    /**
     * Obtém o nome traduzido do bioma
     */
    private static String getBiomeName(Biome biome) {
        switch (biome) {
            case FOREST:
                return "Floresta";
            case PLAINS:
                return "Planície";
            case DESERT:
                return "Deserto";
            case EXTREME_HILLS:
                return "Montanhas";
            case TAIGA:
                return "Taiga";
            case SWAMPLAND:
                return "Pântano";
            case JUNGLE:
                return "Selva";
            case SAVANNA:
                return "Savana";
            case MESA:
                return "Mesa";
            case BIRCH_FOREST:
                return "Floresta de Bétulas";
            case ROOFED_FOREST:
                return "Floresta Escura";
            case ICE_PLAINS:
                return "Planície Gelada";
            case MUSHROOM_ISLAND:
                return "Ilha de Cogumelos";
            case OCEAN:
                return "Oceano";
            case RIVER:
                return "Rio";
            default:
                return biome.name().replace("_", " ");
        }
    }

    /**
     * Lista todos os biomas disponíveis no mundo (para debug)
     */
    public static void listNearbyBiomes(Player p, int radius) {
        Location loc = p.getLocation();
        World world = p.getWorld();

        Map<Biome, Integer> biomeCount = new HashMap<>();

        for (int x = -radius; x <= radius; x += 16) {
            for (int z = -radius; z <= radius; z += 16) {
                Biome biome = world.getBiome(loc.getBlockX() + x, loc.getBlockZ() + z);
                biomeCount.put(biome, biomeCount.getOrDefault(biome, 0) + 1);
            }
        }

        p.sendMessage("§e===== Biomas Próximos =====");
        biomeCount.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .forEach(entry -> {
                    p.sendMessage("§7- §f" + getBiomeName(entry.getKey()) + " §7(" + entry.getValue() + "x)");
                });
    }
}
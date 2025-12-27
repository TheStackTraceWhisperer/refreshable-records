package com.bulletstream.server;

import com.bulletstream.core.GameWorld;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Authoritative Game Server with fixed-tick game loop.
 */
public final class GameServer {
    private static final Logger log = LoggerFactory.getLogger(GameServer.class);
    
    private final GameWorld world;
    private final int tickRate;
    private volatile boolean running;

    public GameServer(int maxEntities, int tickRate) {
        this.world = new GameWorld(maxEntities);
        this.tickRate = tickRate;
        this.running = false;
    }

    public void start() {
        running = true;
        log.info("Game server starting at {} ticks/sec", tickRate);
        
        // Placeholder game loop using nanoTime for precise timing
        long targetFrameTimeNanos = 1_000_000_000L / tickRate;
        
        while (running) {
            long frameStart = System.nanoTime();
            
            // Update game state
            world.update(1.0f / tickRate);
            
            // Sleep to maintain tick rate
            long elapsed = System.nanoTime() - frameStart;
            long sleepTimeNanos = targetFrameTimeNanos - elapsed;
            if (sleepTimeNanos > 0) {
                try {
                    Thread.sleep(sleepTimeNanos / 1_000_000, (int)(sleepTimeNanos % 1_000_000));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        
        log.info("Game server stopped");
    }

    public void stop() {
        running = false;
    }

    public static void main(String[] args) {
        log.info("BulletStream Server - Starting");
        GameServer server = new GameServer(1000, 60);
        
        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
        
        server.start();
    }
}

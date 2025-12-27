package com.bulletstream.server;

import com.bulletstream.core.GameWorld;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Authoritative Game Server with fixed-tick game loop.
 * Uses fixed-step accumulator for deterministic simulation.
 */
public final class GameServer {
    private static final Logger log = LoggerFactory.getLogger(GameServer.class);
    
    private final GameWorld world;
    private final int tickRate;
    private volatile boolean running;
    private long currentTick;

    public GameServer(int maxEntities, int tickRate) {
        this.world = new GameWorld(maxEntities);
        this.tickRate = tickRate;
        this.running = false;
        this.currentTick = 0;
    }

    public void start() {
        running = true;
        log.info("Game server starting at {} ticks/sec", tickRate);
        
        // Fixed-step accumulator loop (using nanoseconds for precision)
        long t = 0L;
        long dtNanos = 1_000_000_000L / tickRate; // Nanoseconds per tick
        long currentTimeNanos = System.nanoTime();
        long accumulatorNanos = 0L;
        
        // Maximum frame time cap (0.25 seconds in nanoseconds)
        final long maxFrameTimeNanos = 250_000_000L;
        
        while (running) {
            long newTimeNanos = System.nanoTime();
            long frameTimeNanos = newTimeNanos - currentTimeNanos;
            currentTimeNanos = newTimeNanos;
            
            // Cap maximum frame time to prevent spiral of death
            if (frameTimeNanos > maxFrameTimeNanos) {
                frameTimeNanos = maxFrameTimeNanos;
            }
            
            accumulatorNanos += frameTimeNanos;
            
            while (accumulatorNanos >= dtNanos) {
                // 1. Drain Network Queue (JCTools) -> Apply Inputs
                // TODO: Process input queue
                
                // 2. Physics Step (GameWorld.update)
                float dtSeconds = dtNanos / 1_000_000_000.0f;
                world.update(dtSeconds);
                
                // 3. Collision Step (SpatialHash)
                // TODO: Implement collision detection
                
                // 4. Pack & Broadcast State (if tick % sendRate == 0)
                // TODO: Broadcast state to clients
                
                t += dtNanos;
                accumulatorNanos -= dtNanos;
                currentTick++;
                
                if (currentTick % (tickRate * 10) == 0) {
                    log.debug("Server tick: {} (t={}s)", currentTick, t / 1_000_000_000.0);
                }
            }
            
            // Small sleep to avoid busy-waiting
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        log.info("Game server stopped after {} ticks", currentTick);
    }

    public void stop() {
        running = false;
    }

    public long getCurrentTick() {
        return currentTick;
    }

    public static void main(String[] args) {
        log.info("BulletStream Server - Starting");
        GameServer server = new GameServer(1000, 60);
        
        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
        
        server.start();
    }
}

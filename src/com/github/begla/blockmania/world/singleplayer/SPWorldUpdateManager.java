/*
 * Copyright 2011 Benjamin Glatzel <benjamin.glatzel@me.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.begla.blockmania.world.singleplayer;

import com.github.begla.blockmania.main.Blockmania;
import com.github.begla.blockmania.world.chunk.Chunk;
import javolution.util.FastList;
import javolution.util.FastSet;

import java.util.Collections;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.logging.Level;

/**
 * Provides support for updating and generating chunks.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public final class SPWorldUpdateManager {

    private final PriorityBlockingQueue<Chunk> _vboUpdates = new PriorityBlockingQueue<Chunk>(64);
    private final FastSet<Chunk> _currentlyProcessedChunks = new FastSet<Chunk>(16);
    /* ------ */
    private int _threadCount = 0, _chunkUpdateAmount;
    private double _meanUpdateDuration = 0.0;
    /* ------ */
    private final SPWorld _parent;

    /**
     * Init. the world update manager.
     *
     * @param _parent The parent world
     */
    public SPWorldUpdateManager(SPWorld _parent) {
        this._parent = _parent;
    }

    /**
     * Retrieves the currently visible chunks from the parent world and updates one dirty/fresh chunk
     * using a new thread. If the thread limit is reached, the new thread is put to sleep until an active thread
     * finished its job.
     */
    public void processChunkUpdates() {
        long timeStart = System.currentTimeMillis();

        // Fetch the currently visible chunks
        final FastList<Chunk> dirtyChunks = new FastList<Chunk>(_parent.fetchVisibleChunks());

        // Remove "okay" chunks
        for (int i = dirtyChunks.size() - 1; i >= 0; i--) {
            Chunk c = dirtyChunks.get(i);

            if (c == null) {
                dirtyChunks.remove(i);
                continue;
            }

            if (!(c.isDirty() || c.isFresh() || c.isLightDirty())) {
                dirtyChunks.remove(i);
            }
        }

        // Nothing to do? Escape!
        if (dirtyChunks.isEmpty()) {
            return;
        }

        // Sort the chunks according to the distance to the world origin (normally the player).
        Collections.sort(dirtyChunks);

        // Retrieve the first chunk...
        final Chunk chunkToProcess = dirtyChunks.getFirst();

        // ... and if this chunk is not updated at the moment...
        if (!_currentlyProcessedChunks.contains(chunkToProcess)) {

            _currentlyProcessedChunks.add(chunkToProcess);

            // ... create a new thread and start processing.
            Thread t = new Thread() {
                @Override
                public void run() {
                    while (_threadCount > Math.max(Runtime.getRuntime().availableProcessors() - 2, 1)) {
                        synchronized (_currentlyProcessedChunks) {
                            try {
                                _currentlyProcessedChunks.wait();
                            } catch (InterruptedException e) {
                                Blockmania.getInstance().getLogger().log(Level.SEVERE, e.getMessage(), e);
                            }
                        }
                    }

                    synchronized (_currentlyProcessedChunks) {
                        _threadCount++;
                    }

                    processChunkUpdate(chunkToProcess);
                    _currentlyProcessedChunks.remove(chunkToProcess);

                    synchronized (_currentlyProcessedChunks) {
                        _threadCount--;
                    }

                    synchronized (_currentlyProcessedChunks) {
                        _currentlyProcessedChunks.notify();
                    }
                }
            };

            t.start();
        }

        _chunkUpdateAmount = dirtyChunks.size();
        _meanUpdateDuration += System.currentTimeMillis() - timeStart;
        _meanUpdateDuration /= 2;
    }

    /**
     * Processes the given chunk and finally queues it for updating the VBOs.
     *
     * @param c The chunk to process
     */
    private void processChunkUpdate(Chunk c) {
        if (c != null) {
            // If the chunk was changed, update the its VBOs.
            if (c.processChunk())
                _vboUpdates.add(c);
        }
    }

    /**
     * Updates the VBOs of all currently queued chunks.
     */
    public void updateVBOs() {
        while (_vboUpdates.size() > 0) {
            Chunk c = _vboUpdates.poll();

            if (c != null)
                c.generateVBOs();
        }
    }

    public int getUpdatesSize() {
        return _chunkUpdateAmount;
    }

    public int getVboUpdatesSize() {
        return _vboUpdates.size();
    }

    public double getMeanUpdateDuration() {
        return _meanUpdateDuration;
    }
}
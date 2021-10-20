/*
 *   Copyright 2020-2021 Rosemoe
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package io.github.rosemoe.editor.struct;

import java.util.Collection;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import io.github.rosemoe.editor.widget.EditorColorScheme;
import org.eclipse.lsp4j.Range;

/**
 * The span model
 *
 * @author Rose
 */
public class Span {
    
    /**
     * Flag for {@link Span#problemFlags}.
     *
     * Indicates this span is in ERROR region
     */
    public static final int FLAG_ERROR = 1 << 4;
    /**
     * Flag for {@link Span#problemFlags}.
     *
     * Indicates this span is in WARNING region
     */
    public static final int FLAG_WARNING = 1 << 3;
    /**
     * Flag for {@link Span#problemFlags}.
     *
     * Indicates this span is in INFO region
     */
    public static final int FLAG_INFO = 1 << 2;
    /**
     * Flag for {@link Span#problemFlags}.
     *
     * Indicates this span is in INFO region
     */
    public static final int FLAG_HINT = 1 << 1;
    /**
     * Flag for {@link Span#problemFlags}.
     *
     * Indicates this span is in DEPRECATED region
     */
    public static final int FLAG_DEPRECATED = 1;
    
    private static final BlockingQueue<Span> cacheQueue = new ArrayBlockingQueue<>(8192 * 2);
    public int line;
    public int column;
    public int colorId;
    public int underlineColor = 0;
    public int problemFlags;
    public Range problemRange;

    /**
     * Create a new span
     *
     * @param line    Start line of span
     * @param column  Start column of span
     * @param colorId Type of span
     * @see Span#obtain(int, int)
     */
    private Span(int line, int column, int colorId) {
        this.line = line;
        this.column = column;
        this.colorId = colorId;
    }

    public static Span obtain(int line, int column, int colorId) {
        Span span = cacheQueue.poll();
        if (span == null) {
            return new Span(line, column, colorId);
        } else {
            span.line = line;
            span.column = column;
            span.colorId = colorId;
            return span;
        }
    }

    public static void recycleAll(Collection<Span> spans) {
        for (Span span : spans) {
            if (!span.recycle()) {
                return;
            }
        }
    }

    /**
     * Set a underline for this region
     * Zero for no underline
     *
     * @param color Color for this underline (not color id of {@link EditorColorScheme})
     * @return Self
     */
    public Span setUnderlineColor(int color) {
        underlineColor = color;
        return this;
    }

    /**
     * Get span start column
     *
     * @return Start column
     */
    public int getColumn() {
        return column;
    }

    /**
     * Set column of this span
     */
    public Span setColumn(int column) {
        this.column = column;
        return this;
    }

    /**
     * Make a copy of this span
     */
    public Span copy() {
        Span copy = obtain(line, column, colorId);
        copy.setUnderlineColor(underlineColor);
        return copy;
    }

    public boolean recycle() {
        colorId = column = underlineColor = 0;
        return cacheQueue.offer(this);
    }

}

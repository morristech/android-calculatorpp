/*
 * Copyright 2013 serso aka se.solovyev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * Contact details
 *
 * Email: se.solovyev@gmail.com
 * Site:  http://se.solovyev.org
 */

package org.solovyev.android.calculator;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.ContextMenu;
import org.solovyev.android.Check;
import org.solovyev.android.calculator.view.EditTextCompat;
import org.solovyev.android.views.Adjuster;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EditorView extends EditTextCompat {

    private boolean editorChange;
    @Nullable
    private Editor editor;

    public EditorView(Context context) {
        super(context);
        init();
    }

    public EditorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public EditorView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        if (!App.isFloatingCalculator(getContext())) {
            Adjuster.adjustText(this, 0.22f,
                    getResources().getDimensionPixelSize(R.dimen.cpp_min_editor_text_size));
        }
        addTextChangedListener(new MyTextWatcher());
        dontShowSoftInputOnFocusCompat();
        // the state is controlled by Editor
        setSaveEnabled(false);
    }

    public void setEditor(@Nullable Editor editor) {
        if (this.editor == editor) {
            return;
        }
        if (editor != null) {
            // avoid losing cursor position on focus restore. First request focus, then set cursor
            // position. Consequent requestFocus() should be no-op
            requestFocus();
            setState(editor.getState());
        }
        // update editor at the end to avoid side-effects of #requestFocus() and #setState()
        this.editor = editor;
    }

    @Override
    protected void onCreateContextMenu(ContextMenu menu) {
        super.onCreateContextMenu(menu);
        menu.removeItem(android.R.id.selectAll);
    }

    public void setState(@Nonnull final EditorState state) {
        Check.isMainThread();
        // we don't want to be notified about changes we make ourselves
        editorChange = true;
        if (App.getTheme().light && App.isFloatingCalculator(getContext())) {
            // don't need formatting
            setText(state.getTextString());
        } else {
            setText(state.text, BufferType.EDITABLE);
        }
        editorChange = false;
        setSelection(Editor.clamp(state.selection, length()));
    }

    @Override
    protected void onSelectionChanged(int start, int end) {
        Check.isMainThread();
        super.onSelectionChanged(start, end);
        if (start != end) {
            return;
        }
        if (editor == null || editorChange) {
            return;
        }
        editor.setSelection(start);
    }

    private class MyTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (editor == null || editorChange) {
                return;
            }
            editor.setText(String.valueOf(s));
        }
    }
}

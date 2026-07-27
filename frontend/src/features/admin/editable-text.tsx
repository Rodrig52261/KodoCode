"use client";

import { type ClipboardEvent, useEffect, useRef } from "react";

export function EditableText({ value, label, multiline = false, onChange }: Readonly<{
  value: string;
  label: string;
  multiline?: boolean;
  onChange: (value: string) => void;
}>) {
  const ref = useRef<HTMLSpanElement>(null);

  useEffect(() => {
    if (ref.current && document.activeElement !== ref.current && ref.current.innerText !== value) {
      ref.current.innerText = value;
    }
  }, [value]);

  function pastePlainText(event: ClipboardEvent<HTMLSpanElement>) {
    event.preventDefault();
    const plainText = event.clipboardData.getData("text/plain").slice(0, 4000);
    const selection = window.getSelection();
    if (!selection?.rangeCount) return;
    const range = selection.getRangeAt(0);
    range.deleteContents();
    range.insertNode(document.createTextNode(plainText));
    range.collapse(false);
    selection.removeAllRanges();
    selection.addRange(range);
  }

  return (
    <span
      aria-label={`Editar ${label}`}
      className="inline-editable-text"
      contentEditable="plaintext-only"
      data-placeholder={`Digite ${label.toLowerCase()}`}
      ref={ref}
      role="textbox"
      spellCheck
      suppressContentEditableWarning
      tabIndex={0}
      onBlur={(event) => onChange(event.currentTarget.innerText.replace(/\n{3,}/g, "\n\n").trim())}
      onKeyDown={(event) => {
        if (!multiline && event.key === "Enter") {
          event.preventDefault();
          event.currentTarget.blur();
        }
        if (event.key === "Escape") {
          event.currentTarget.innerText = value;
          event.currentTarget.blur();
        }
      }}
      onDrop={(event) => event.preventDefault()}
      onPaste={pastePlainText}
    >
      {value}
    </span>
  );
}

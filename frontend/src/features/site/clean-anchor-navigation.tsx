"use client";

import { useEffect } from "react";

function cleanUrl() {
  window.history.replaceState(window.history.state, "", `${window.location.pathname}${window.location.search}`);
}

function findSection(hash: string) {
  if (!hash.startsWith("#") || hash.length === 1) return null;
  try {
    return document.getElementById(decodeURIComponent(hash.slice(1)));
  } catch {
    return null;
  }
}

export function CleanAnchorNavigation() {
  useEffect(() => {
    const initialSection = findSection(window.location.hash);
    if (initialSection) {
      window.requestAnimationFrame(() => {
        initialSection.scrollIntoView();
        cleanUrl();
      });
    }

    function handleClick(event: MouseEvent) {
      if (event.defaultPrevented || event.button !== 0 || event.metaKey || event.ctrlKey || event.shiftKey || event.altKey) return;
      const anchor = (event.target as Element | null)?.closest<HTMLAnchorElement>('a[href^="#"]');
      if (!anchor || anchor.target === "_blank") return;

      const section = findSection(anchor.getAttribute("href") ?? "");
      if (!section) return;

      event.preventDefault();
      section.scrollIntoView({ behavior: "smooth", block: "start" });
      cleanUrl();
    }

    document.addEventListener("click", handleClick);
    return () => document.removeEventListener("click", handleClick);
  }, []);

  return null;
}

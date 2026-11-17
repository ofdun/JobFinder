import { useEffect, useState } from "react";
import { Icon } from "./Icon";

const STORAGE_KEY = "jobfinder.theme";
type Theme = "light" | "dark";

function applyTheme(theme: Theme) {
  document.documentElement.dataset.theme = theme;
  document.documentElement.style.colorScheme = theme;
}

export function ThemeToggle() {
  const [theme, setTheme] = useState<Theme>(() =>
    typeof document !== "undefined" &&
    document.documentElement.dataset.theme === "dark"
      ? "dark"
      : "light",
  );

  useEffect(() => {
    const media = window.matchMedia("(prefers-color-scheme: dark)");
    const syncPreference = () => {
      let saved: string | null = null;
      try {
        saved = localStorage.getItem(STORAGE_KEY);
      } catch {
        /* Storage may be disabled. */
      }
      const next =
        saved === "light" || saved === "dark"
          ? saved
          : media.matches
            ? "dark"
            : "light";
      applyTheme(next);
      setTheme(next);
    };
    const onStorage = (event: StorageEvent) => {
      if (event.key === STORAGE_KEY || event.key === null) syncPreference();
    };
    syncPreference();
    media.addEventListener("change", syncPreference);
    window.addEventListener("storage", onStorage);
    return () => {
      media.removeEventListener("change", syncPreference);
      window.removeEventListener("storage", onStorage);
    };
  }, []);

  const toggle = () => {
    const next = theme === "dark" ? "light" : "dark";
    applyTheme(next);
    setTheme(next);
    try {
      localStorage.setItem(STORAGE_KEY, next);
    } catch {
      /* The toggle still works without persistence. */
    }
  };

  return (
    <button
      type="button"
      className="theme-toggle"
      onClick={toggle}
      aria-label={
        theme === "dark" ? "Включить светлую тему" : "Включить тёмную тему"
      }
      title={theme === "dark" ? "Светлая тема" : "Тёмная тема"}
    >
      <Icon name={theme === "dark" ? "sun" : "moon"} size={19} />
    </button>
  );
}

import { Injectable, signal } from '@angular/core';

type ThemeMode = 'light' | 'dark';

const STORAGE_KEY = 'theme-preference';

@Injectable({ providedIn: 'root' })
export class ThemeService {
  readonly isDark = signal(this.computeInitialIsDark());

  constructor() {
    this.applyToDocument(this.getStoredMode());
  }

  toggle(): void {
    const next: ThemeMode = this.isDark() ? 'light' : 'dark';
    this.isDark.set(next === 'dark');
    localStorage.setItem(STORAGE_KEY, next);
    this.applyToDocument(next);
  }

  private getStoredMode(): ThemeMode | null {
    const stored = localStorage.getItem(STORAGE_KEY);
    return stored === 'dark' || stored === 'light' ? stored : null;
  }

  private computeInitialIsDark(): boolean {
    const stored = this.getStoredMode();
    if (stored) return stored === 'dark';
    return window.matchMedia('(prefers-color-scheme: dark)').matches;
  }

  private applyToDocument(mode: ThemeMode | null): void {
    if (mode) {
      document.documentElement.setAttribute('data-theme', mode);
    } else {
      document.documentElement.removeAttribute('data-theme');
    }
  }
}

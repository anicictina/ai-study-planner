import { Component, OnInit, inject, signal } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { forkJoin } from 'rxjs';
import { AuthService } from '../core/services/auth.service';
import { AvailabilitySlotRequest, DayOfWeek } from '../core/models/profile.model';
import { ProfileService } from '../core/services/profile.service';

const DAYS: { value: DayOfWeek; label: string }[] = [
  { value: 'MONDAY', label: 'Ponedeljak' },
  { value: 'TUESDAY', label: 'Utorak' },
  { value: 'WEDNESDAY', label: 'Sreda' },
  { value: 'THURSDAY', label: 'Četvrtak' },
  { value: 'FRIDAY', label: 'Petak' },
  { value: 'SATURDAY', label: 'Subota' },
  { value: 'SUNDAY', label: 'Nedelja' }
];

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatCheckboxModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule
  ],
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.css'
})
export class ProfileComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly profileService = inject(ProfileService);
  private readonly authService = inject(AuthService);

  readonly days = DAYS;
  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly successMessage = signal<string | null>(null);
  readonly errorMessage = signal<string | null>(null);

  readonly form = this.fb.group({
    preferredStudyTime: [this.authService.currentUser()?.preferredStudyTime ?? null],
    days: this.fb.array(
      DAYS.map(() =>
        this.fb.group({
          enabled: [false],
          startTime: ['17:00'],
          endTime: ['20:00']
        })
      )
    )
  });

  get daysArray(): FormArray<FormGroup> {
    return this.form.controls.days as FormArray<FormGroup>;
  }

  ngOnInit(): void {
    this.profileService.getAvailability().subscribe({
      next: (slots) => {
        DAYS.forEach((day, index) => {
          const slot = slots.find((s) => s.dayOfWeek === day.value);
          if (slot) {
            this.daysArray.at(index).patchValue({
              enabled: true,
              startTime: slot.startTime.slice(0, 5),
              endTime: slot.endTime.slice(0, 5)
            });
          }
        });
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  submit(): void {
    this.errorMessage.set(null);
    this.successMessage.set(null);

    const dayValues = this.daysArray.value as { enabled: boolean; startTime: string; endTime: string }[];
    const slots: AvailabilitySlotRequest[] = [];

    for (let i = 0; i < DAYS.length; i++) {
      const value = dayValues[i];
      if (!value.enabled) continue;

      if (value.startTime >= value.endTime) {
        this.errorMessage.set(`Kraj mora biti posle početka za ${DAYS[i].label}`);
        return;
      }

      slots.push({
        dayOfWeek: DAYS[i].value,
        startTime: `${value.startTime}:00`,
        endTime: `${value.endTime}:00`
      });
    }

    this.saving.set(true);

    forkJoin({
      availability: this.profileService.updateAvailability(slots),
      user: this.profileService.updatePreferredTime(this.form.controls.preferredStudyTime.value)
    }).subscribe({
      next: ({ user }) => {
        this.authService.updateCurrentUser(user);
        this.saving.set(false);
        this.successMessage.set('Podešavanja su sačuvana.');
      },
      error: (err) => {
        this.saving.set(false);
        this.errorMessage.set(err?.error?.message ?? 'Čuvanje podešavanja nije uspelo.');
      }
    });
  }
}

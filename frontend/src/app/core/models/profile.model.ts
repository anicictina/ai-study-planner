export type DayOfWeek =
  | 'MONDAY'
  | 'TUESDAY'
  | 'WEDNESDAY'
  | 'THURSDAY'
  | 'FRIDAY'
  | 'SATURDAY'
  | 'SUNDAY';

export type PreferredTime = 'MORNING' | 'AFTERNOON' | 'EVENING';

export interface AvailabilitySlot {
  id: number;
  dayOfWeek: DayOfWeek;
  startTime: string;
  endTime: string;
}

export interface AvailabilitySlotRequest {
  dayOfWeek: DayOfWeek;
  startTime: string;
  endTime: string;
}

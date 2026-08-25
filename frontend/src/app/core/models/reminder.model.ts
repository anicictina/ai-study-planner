export interface Reminder {
  id: number;
  subjectId: number | null;
  subjectName: string | null;
  subjectColor: string | null;
  message: string;
  remindAt: string;
  dismissed: boolean;
  due: boolean;
  createdAt: string;
}

export interface ReminderRequest {
  subjectId?: number | null;
  message: string;
  remindAt: string;
}

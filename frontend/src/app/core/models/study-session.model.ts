export type ActivityType = 'READING' | 'PRACTICE' | 'QUIZ' | 'REVISION' | 'HOMEWORK';

export interface StudySession {
  id: number;
  subjectId: number;
  subjectName: string;
  subjectColor: string;
  topic: string | null;
  sessionDate: string;
  startTime: string | null;
  durationMinutes: number;
  completed: boolean;
  activityType: ActivityType;
}

export interface StudySessionRequest {
  subjectId: number;
  topic?: string | null;
  sessionDate: string;
  startTime?: string | null;
  durationMinutes: number;
  activityType?: ActivityType;
}

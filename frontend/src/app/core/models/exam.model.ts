export type ExamStatus = 'PLANNED' | 'PASSED' | 'FAILED';

export interface Exam {
  id: number;
  subjectId: number;
  subjectName: string;
  subjectColor: string;
  examDate: string;
  examTime: string | null;
  location: string | null;
  status: ExamStatus;
  daysRemaining: number;
}

export interface ExamRequest {
  subjectId: number;
  examDate: string;
  examTime?: string | null;
  location?: string | null;
  status?: ExamStatus;
}

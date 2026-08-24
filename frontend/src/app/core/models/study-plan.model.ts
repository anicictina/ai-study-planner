export type StudyPlanStatus = 'PENDING' | 'ACCEPTED' | 'DISCARDED';

export interface StudyPlanItem {
  id: number;
  subjectId: number;
  subjectName: string;
  subjectColor: string;
  itemDate: string;
  startTime: string;
  durationMinutes: number;
  topic: string | null;
}

export interface StudyPlan {
  id: number;
  generatedAt: string;
  status: StudyPlanStatus;
  rejectedItemsCount: number;
  validationNotes: string | null;
  items: StudyPlanItem[];
}

export interface StudyPlanGenerateRequest {
  subjectIds?: number[];
}

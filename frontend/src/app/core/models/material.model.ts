export type MaterialStatus = 'NOT_STARTED' | 'IN_PROGRESS' | 'LEARNED';

export interface StudyMaterial {
  id: number;
  subjectId: number;
  subjectName: string;
  title: string;
  content: string;
  status: MaterialStatus;
  createdAt: string;
}

export interface MaterialRequest {
  subjectId: number;
  title: string;
  content: string;
}

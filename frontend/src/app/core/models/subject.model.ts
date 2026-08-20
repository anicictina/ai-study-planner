export type Level = 'LOW' | 'MEDIUM' | 'HIGH';

export interface Subject {
  id: number;
  name: string;
  description: string | null;
  credits: number;
  difficulty: Level;
  priority: Level;
  knowledgePercent: number;
  color: string;
  archived: boolean;
  createdAt: string;
}

export interface SubjectRequest {
  name: string;
  description?: string | null;
  credits: number;
  difficulty: Level;
  priority: Level;
  knowledgePercent?: number;
  color?: string;
}

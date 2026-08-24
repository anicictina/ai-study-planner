export interface QuizQuestion {
  id: number;
  questionText: string;
  options: string[];
}

export interface Quiz {
  id: number;
  materialId: number;
  materialTitle: string;
  createdAt: string;
  questions: QuizQuestion[];
}

export interface QuestionAnswer {
  questionId: number;
  selectedIndex: number;
}

export interface QuestionResult {
  questionId: number;
  questionText: string;
  options: string[];
  selectedIndex: number | null;
  correctAnswerIndex: number;
  correct: boolean;
  explanation: string;
}

export interface QuizResult {
  quizId: number;
  correctCount: number;
  totalCount: number;
  results: QuestionResult[];
}

export interface QuizAttemptSummary {
  id: number;
  quizId: number;
  materialId: number;
  materialTitle: string;
  correctCount: number;
  totalCount: number;
  attemptedAt: string;
}

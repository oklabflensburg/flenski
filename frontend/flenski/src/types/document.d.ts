export interface Document {
  identifier: string;
  title?: string;
  description?: string;
  summary?: string;
  url: string;
  sourceDateTime?: string;
  discoveryDateTime: string;
  type: 'PDF' | 'Website';
  group: string;
  categories?: string[];
  content?: string;
}


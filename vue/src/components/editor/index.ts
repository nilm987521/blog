import { MdEditor, MdPreview } from 'md-editor-v3';
import MarkdownEditor from './MarkdownEditor.vue';
import 'md-editor-v3/lib/style.css';

// 導出原生 md-editor-v3 元件
export { MdEditor, MdPreview };

// 導出自定義的 MarkdownEditor 元件
export { MarkdownEditor };

// 默認導出我們的 MarkdownEditor
export default MarkdownEditor;

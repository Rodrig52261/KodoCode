import { ContentEditor } from "@/features/admin/content-editor";
export default async function EditorPage({params}:{params:Promise<{sectionKey:string}>}){return <ContentEditor sectionKey={(await params).sectionKey}/>}

import { Brand } from "./brand";
import { CleanAnchorNavigation } from "./clean-anchor-navigation";
import type { PublicFaq, SiteSections } from "./content-schema";
import { Icon } from "./icon";
import { OrganizationSchema } from "./schema-org";
import { SiteHeader } from "./site-header";
import { EditableText } from "@/features/admin/editable-text";
import { ContactForm } from "@/features/contact/contact-form";

type ContentPath = Array<string | number>;
export type LandingPageEditor = {
  onSectionTextChange: (section: keyof SiteSections, path: ContentPath, value: string) => void;
  onFaqTextChange: (id: string, field: "question" | "answer", value: string) => void;
};

type SectionIntroProps = {
  section: keyof SiteSections;
  eyebrow: string;
  title: string;
  description?: string;
  centered?: boolean;
  editor?: LandingPageEditor;
};

function Editable({ editor, section, path, value, label, multiline = false }: Readonly<{
  editor?: LandingPageEditor;
  section: keyof SiteSections;
  path: ContentPath;
  value: string;
  label: string;
  multiline?: boolean;
}>) {
  if (!editor) return value;
  return <EditableText value={value} label={label} multiline={multiline} onChange={(next) => editor.onSectionTextChange(section, path, next)} />;
}

function SectionIntro({ section, eyebrow, title, description, centered = false, editor }: Readonly<SectionIntroProps>) {
  return (
    <div className={`max-w-3xl ${centered ? "mx-auto text-center" : ""}`}>
      <p className="eyebrow"><Editable editor={editor} section={section} path={["eyebrow"]} value={eyebrow} label={`texto de apoio de ${section}`} /></p>
      <h2 className="section-title mt-4"><Editable editor={editor} section={section} path={["title"]} value={title} label={`título de ${section}`} /></h2>
      {description && <p className="section-description mt-5"><Editable editor={editor} section={section} path={["description"]} value={description} label={`descrição de ${section}`} multiline /></p>}
    </div>
  );
}

function HeroVisual({ content, editor }: Readonly<{ content: SiteSections["hero"]["visual"]; editor?: LandingPageEditor }>) {
  const icons = ["message", "users", "automation"] as const;
  const tones = ["bg-sky-400/10 text-sky-300", "bg-blue-400/10 text-blue-300", "bg-violet-400/10 text-violet-300"];
  return (
    <div className="hero-visual" aria-label="Representação de processos digitais conectados" role="img">
      <div className="hero-grid" aria-hidden="true" />
      <div className="relative z-10 mx-auto w-full max-w-md">
        <div className="rounded-2xl border border-sky-200/15 bg-[#0d1b42]/95 p-4 shadow-2xl shadow-slate-950/30">
          <div className="flex items-center justify-between border-b border-white/10 pb-3">
            <div className="flex gap-1.5"><span className="size-2 rounded-full bg-sky-300"/><span className="size-2 rounded-full bg-blue-400"/><span className="size-2 rounded-full bg-violet-400"/></div>
            <span className="text-[0.65rem] font-semibold uppercase tracking-[0.2em] text-slate-400"><Editable editor={editor} section="hero" path={["visual", "eyebrow"]} value={content.eyebrow} label="texto do fluxo visual" /></span>
          </div>
          <div className="mt-5 grid gap-3">
            {content.steps.map((step, index) => (
              <div className="contents" key={`${step.title}-${index}`}>
                {index > 0 && <div className="mx-auto h-4 w-px bg-gradient-to-b from-sky-300/70 to-violet-300/70" />}
                <div className={`workflow-card ${index % 2 === 0 ? "translate-x-2" : "-translate-x-2"}`}>
                  <span className={`workflow-icon ${tones[index] ?? tones[2]}`}><Icon name={icons[index] ?? "automation"} className="size-4" /></span>
                  <span><strong><Editable editor={editor} section="hero" path={["visual", "steps", index, "title"]} value={step.title} label={`título da etapa visual ${index + 1}`} /></strong><small><Editable editor={editor} section="hero" path={["visual", "steps", index, "description"]} value={step.description} label={`descrição da etapa visual ${index + 1}`} /></small></span>
                  <span className="status-dot" />
                </div>
              </div>
            ))}
          </div>
        </div>
        <div className="absolute -bottom-5 -left-5 rounded-2xl border border-white/15 bg-white px-4 py-3 text-[var(--ink)] shadow-xl">
          <span className="flex items-center gap-2 text-xs font-bold"><span className="grid size-6 place-items-center rounded-full bg-gradient-to-br from-sky-100 to-violet-100 text-blue-700"><Icon name="check" className="size-3.5" /></span><Editable editor={editor} section="hero" path={["visual", "status"]} value={content.status} label="status do fluxo visual" /></span>
        </div>
      </div>
    </div>
  );
}

export function LandingPage({ sections, faqs, preview = false, editor }: Readonly<{ sections: SiteSections; faqs: PublicFaq[]; preview?: boolean; editor?: LandingPageEditor }>) {
  const currentYear = new Date().getFullYear();

  return (
    <div className={editor ? "landing-editor" : preview ? "landing-preview pointer-events-none select-none" : undefined}>
      {!preview && !editor && <OrganizationSchema sections={sections} />}
      {!preview && !editor && <CleanAnchorNavigation />}
      <SiteHeader navigation={sections.navigation} onTextChange={editor ? (path, value) => editor.onSectionTextChange("navigation", path, value) : undefined} />

      <main id={preview || editor ? undefined : "conteudo-principal"}>
        <section className="hero-section scroll-mt-24" id="inicio">
          <div className="site-container grid items-center gap-14 py-18 lg:grid-cols-[1.08fr_.92fr] lg:py-24 xl:gap-20 xl:py-28">
            <div>
              <p className="hero-eyebrow eyebrow eyebrow-light"><Editable editor={editor} section="hero" path={["eyebrow"]} value={sections.hero.eyebrow} label="texto de apoio principal" /></p>
              <h1 className="hero-title mt-5 max-w-3xl text-4xl font-bold leading-[1.08] tracking-[-0.045em] text-white sm:text-5xl lg:text-[4rem]">
                <Editable editor={editor} section="hero" path={["title"]} value={sections.hero.title} label="título principal" multiline />
              </h1>
              <p className="mt-6 max-w-2xl text-lg leading-8 text-slate-300 lg:text-xl"><Editable editor={editor} section="hero" path={["description"]} value={sections.hero.description} label="descrição principal" multiline /></p>
              <div className="mt-8 flex flex-col gap-3 sm:flex-row">
                <a className="button-accent" href={editor ? undefined : sections.hero.primaryCta.href}><Editable editor={editor} section="hero" path={["primaryCta", "label"]} value={sections.hero.primaryCta.label} label="botão principal" /><Icon name="arrow" className="size-5" /></a>
                <a className="button-ghost" href={editor ? undefined : sections.hero.secondaryCta.href}><Editable editor={editor} section="hero" path={["secondaryCta", "label"]} value={sections.hero.secondaryCta.label} label="botão secundário" /></a>
              </div>
              <ul className="mt-9 flex flex-wrap gap-x-6 gap-y-3" aria-label="Destaques">
                {sections.hero.highlights.map((highlight, index) => (
                  <li className="flex items-center gap-2 text-sm font-medium text-slate-300" key={highlight}>
                    <span className="grid size-5 place-items-center rounded-full bg-white/10 text-[var(--accent)]"><Icon name="check" className="size-3" /></span>
                    <Editable editor={editor} section="hero" path={["highlights", index]} value={highlight} label={`destaque ${index + 1}`} />
                  </li>
                ))}
              </ul>
            </div>
            <HeroVisual content={sections.hero.visual} editor={editor} />
          </div>
        </section>

        <section className="section-padding bg-white" aria-labelledby="benefits-title">
          <div className="site-container">
            <div id="benefits-title">
              <SectionIntro section="benefits" eyebrow={sections.benefits.eyebrow} title={sections.benefits.title} description={sections.benefits.description} centered editor={editor} />
            </div>
            <div className="mt-12 grid gap-px overflow-hidden rounded-3xl border border-[var(--border)] bg-[var(--border)] shadow-[0_1.25rem_3rem_rgb(30_64_175_/_6%)] sm:grid-cols-2 lg:grid-cols-4">
              {sections.benefits.items.map((item, index) => (
                <article className="group bg-white p-6 transition hover:bg-[var(--soft)] lg:p-7" key={item.title}>
                  <span className="icon-box"><Icon name={item.icon} /></span>
                  <h3 className="mt-5 text-lg font-bold tracking-tight text-[var(--ink)]"><Editable editor={editor} section="benefits" path={["items", index, "title"]} value={item.title} label={`título do benefício ${index + 1}`} /></h3>
                  <p className="mt-3 text-sm leading-6 text-slate-600"><Editable editor={editor} section="benefits" path={["items", index, "description"]} value={item.description} label={`descrição do benefício ${index + 1}`} multiline /></p>
                </article>
              ))}
            </div>
          </div>
        </section>

        <section className="section-padding scroll-mt-20 bg-[var(--background)]" id="solucoes">
          <div className="site-container">
            <SectionIntro section="services" eyebrow={sections.services.eyebrow} title={sections.services.title} description={sections.services.description} editor={editor} />
            <div className="mt-12 grid gap-5 md:grid-cols-2 xl:grid-cols-3">
              {sections.services.items.map((service, index) => (
                <article className={`service-card ${index === sections.services.items.length - 1 ? "md:col-span-2 xl:col-span-1" : ""}`} key={service.name}>
                  <div className="flex items-start justify-between gap-4">
                    <span className="icon-box icon-box-dark"><Icon name={service.icon} /></span>
                    <span className="text-xs font-bold tracking-[0.16em] text-slate-400">0{index + 1}</span>
                  </div>
                  <h3 className="mt-7 text-2xl font-bold tracking-[-0.03em] text-[var(--ink)]"><Editable editor={editor} section="services" path={["items", index, "name"]} value={service.name} label={`nome da solução ${index + 1}`} /></h3>
                  <p className="mt-4 leading-7 text-slate-600"><Editable editor={editor} section="services" path={["items", index, "description"]} value={service.description} label={`descrição da solução ${index + 1}`} multiline /></p>
                  <ul className="mt-6 space-y-3">
                    {service.benefits.map((benefit, benefitIndex) => (
                      <li className="flex items-center gap-3 text-sm font-medium text-slate-700" key={benefit}>
                        <span className="grid size-5 shrink-0 place-items-center rounded-full bg-gradient-to-br from-sky-100 to-violet-100 text-blue-700"><Icon name="check" className="size-3" /></span>
                        <Editable editor={editor} section="services" path={["items", index, "benefits", benefitIndex]} value={benefit} label={`benefício ${benefitIndex + 1} da solução ${index + 1}`} />
                      </li>
                    ))}
                  </ul>
                  <a className="mt-7 inline-flex items-center gap-2 text-sm font-bold text-[var(--primary-dark)] hover:text-[var(--primary)]" href={editor ? undefined : "#contato"}>
                    <Editable editor={editor} section="services" path={["items", index, "ctaLabel"]} value={service.ctaLabel} label={`botão da solução ${index + 1}`} /><Icon name="arrow" className="size-4" />
                  </a>
                </article>
              ))}
            </div>
          </div>
        </section>

        <section className="section-padding scroll-mt-20 bg-white" id="processo">
          <div className="site-container">
            <SectionIntro section="process" eyebrow={sections.process.eyebrow} title={sections.process.title} description={sections.process.description} centered editor={editor} />
            <ol className="relative mt-14 grid gap-5 md:grid-cols-2 lg:grid-cols-4">
              {sections.process.items.map((item, index) => (
                <li className={`process-card ${index === sections.process.items.length - 1 ? "md:col-span-2 lg:col-span-1" : ""}`} key={item.number}>
                  <span className="process-number">{item.number}</span>
                  <h3 className="mt-6 text-lg font-bold text-[var(--ink)]"><Editable editor={editor} section="process" path={["items", index, "title"]} value={item.title} label={`título da etapa ${index + 1}`} /></h3>
                  <p className="mt-3 text-sm leading-6 text-slate-600"><Editable editor={editor} section="process" path={["items", index, "description"]} value={item.description} label={`descrição da etapa ${index + 1}`} multiline /></p>
                </li>
              ))}
            </ol>
          </div>
        </section>

        <section className="differentials-section section-padding bg-[var(--ink)] text-white">
          <div className="site-container grid gap-12 lg:grid-cols-[.8fr_1.2fr] lg:gap-20">
            <div>
              <p className="eyebrow eyebrow-light"><Editable editor={editor} section="differentials" path={["eyebrow"]} value={sections.differentials.eyebrow} label="texto de apoio dos diferenciais" /></p>
              <h2 className="section-title mt-4 text-white"><Editable editor={editor} section="differentials" path={["title"]} value={sections.differentials.title} label="título dos diferenciais" /></h2>
              <div className="mt-8 h-1 w-16 rounded-full bg-[var(--accent)]" />
            </div>
            <div className="grid gap-x-8 gap-y-8 sm:grid-cols-2">
              {sections.differentials.items.map((item, index) => (
                <article className="border-t border-white/15 pt-5" key={item.title}>
                  <div className="flex items-start gap-4">
                    <span className="font-mono text-sm font-bold text-[var(--accent)]">0{index + 1}</span>
                    <div>
                      <h3 className="font-bold text-white"><Editable editor={editor} section="differentials" path={["items", index, "title"]} value={item.title} label={`título do diferencial ${index + 1}`} /></h3>
                      <p className="mt-2 text-sm leading-6 text-slate-300"><Editable editor={editor} section="differentials" path={["items", index, "description"]} value={item.description} label={`descrição do diferencial ${index + 1}`} multiline /></p>
                    </div>
                  </div>
                </article>
              ))}
            </div>
          </div>
        </section>

        <section className="section-padding scroll-mt-20 bg-[var(--warm)]" id="sobre">
          <div className="site-container grid gap-12 lg:grid-cols-[1.05fr_.95fr] lg:items-start lg:gap-20">
            <div>
              <SectionIntro section="about" eyebrow={sections.about.eyebrow} title={sections.about.title} editor={editor} />
              <div className="mt-6 space-y-4 text-lg leading-8 text-slate-600">
                {sections.about.paragraphs.map((paragraph, index) => <p key={`${paragraph}-${index}`}><Editable editor={editor} section="about" path={["paragraphs", index]} value={paragraph} label={`parágrafo sobre a empresa ${index + 1}`} multiline /></p>)}
              </div>
            </div>
            <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-1">
              <article className="about-card">
                <span className="about-label"><Editable editor={editor} section="about" path={["missionLabel"]} value={sections.about.missionLabel} label="título da missão" /></span>
                <p><Editable editor={editor} section="about" path={["mission"]} value={sections.about.mission} label="missão" multiline /></p>
              </article>
              <article className="about-card">
                <span className="about-label"><Editable editor={editor} section="about" path={["visionLabel"]} value={sections.about.visionLabel} label="título da visão" /></span>
                <p><Editable editor={editor} section="about" path={["vision"]} value={sections.about.vision} label="visão" multiline /></p>
              </article>
              <article className="about-card sm:col-span-2 lg:col-span-1">
                <span className="about-label"><Editable editor={editor} section="about" path={["valuesLabel"]} value={sections.about.valuesLabel} label="título dos valores" /></span>
                <ul className="mt-4 flex flex-wrap gap-2">
                  {sections.about.values.map((value, index) => <li className="rounded-full border border-violet-100 bg-white px-3 py-1.5 text-sm font-semibold text-slate-700 shadow-sm" key={`${value}-${index}`}><Editable editor={editor} section="about" path={["values", index]} value={value} label={`valor ${index + 1}`} /></li>)}
                </ul>
              </article>
            </div>
          </div>
        </section>

        <section className="section-padding bg-white" id="perguntas-frequentes">
          <div className="site-container grid gap-12 lg:grid-cols-[.72fr_1.28fr] lg:gap-20">
            <div>
              <p className="eyebrow"><Editable editor={editor} section="faq" path={["eyebrow"]} value={sections.faq.eyebrow} label="texto de apoio das perguntas" /></p>
              <h2 className="section-title mt-4"><Editable editor={editor} section="faq" path={["title"]} value={sections.faq.title} label="título das perguntas" /></h2>
              <p className="section-description mt-5"><Editable editor={editor} section="faq" path={["description"]} value={sections.faq.description} label="descrição das perguntas" multiline /></p>
            </div>
            <div className="divide-y divide-slate-200 border-y border-slate-200">
              {faqs.map((faq, index) => (
                <details className="faq-item group" key={faq.id} open={index === 0}>
                  <summary className="flex cursor-pointer list-none items-center justify-between gap-5 py-5 font-bold text-[var(--ink)]">
                    {editor ? <EditableText value={faq.question} label={`pergunta ${index + 1}`} onChange={(value) => editor.onFaqTextChange(faq.id, "question", value)} /> : faq.question}
                    <span aria-hidden="true" className="faq-toggle">+</span>
                  </summary>
                  <p className="max-w-2xl pb-6 pr-10 leading-7 text-slate-600">{editor ? <EditableText value={faq.answer} label={`resposta ${index + 1}`} multiline onChange={(value) => editor.onFaqTextChange(faq.id, "answer", value)} /> : faq.answer}</p>
                </details>
              ))}
            </div>
          </div>
        </section>

        <section className="bg-white px-4 pb-5 sm:px-6 sm:pb-6">
          <div className="site-container cta-panel">
            <div className="relative z-10 max-w-3xl">
              <p className="eyebrow eyebrow-light"><Editable editor={editor} section="cta" path={["eyebrow"]} value={sections.cta.eyebrow} label="texto de apoio da chamada final" /></p>
              <h2 className="mt-4 text-3xl font-bold tracking-[-0.04em] text-white sm:text-4xl lg:text-5xl"><Editable editor={editor} section="cta" path={["title"]} value={sections.cta.title} label="título da chamada final" /></h2>
              <p className="mt-5 max-w-2xl text-lg leading-8 text-slate-300"><Editable editor={editor} section="cta" path={["description"]} value={sections.cta.description} label="descrição da chamada final" multiline /></p>
              <a className="button-accent mt-8" href={editor ? undefined : sections.cta.buttonHref}><Editable editor={editor} section="cta" path={["buttonLabel"]} value={sections.cta.buttonLabel} label="botão da chamada final" /><Icon name="arrow" className="size-5" /></a>
            </div>
          </div>
        </section>

        <section className="contact-section section-padding scroll-mt-20 bg-[var(--background)]" id="contato">
          <div className="site-container grid items-start gap-10 lg:grid-cols-[.75fr_1.25fr] lg:gap-16">
            <div className="lg:sticky lg:top-28">
              <SectionIntro section="contact" eyebrow={sections.contact.eyebrow} title={sections.contact.title} description={sections.contact.description} editor={editor} />
              <p className="mt-7 text-sm text-slate-600"><Editable editor={editor} section="contact" path={["emailPrompt"]} value={sections.contact.emailPrompt} label="chamada do e-mail" /> <a className="font-bold text-[var(--primary-dark)]" href={editor ? undefined : `mailto:${sections.contact.email}`}><Editable editor={editor} section="contact" path={["email"]} value={sections.contact.email} label="e-mail de contato" /></a></p>
              <p className="mt-3 flex items-center gap-2 text-sm text-slate-500"><span className="size-2 rounded-full bg-emerald-500" /><Editable editor={editor} section="contact" path={["responseTime"]} value={sections.contact.responseTime} label="prazo de resposta" /></p>
            </div>
            <ContactForm />
          </div>
        </section>
      </main>

      <footer className="bg-[#070d24] text-slate-300">
        <div className="site-container grid gap-10 py-14 md:grid-cols-2 lg:grid-cols-[1.15fr_.85fr_.85fr] lg:py-16">
          <div className="max-w-sm">
            <Brand inverted />
            <p className="mt-5 text-sm leading-6 text-slate-400"><Editable editor={editor} section="footer" path={["description"]} value={sections.footer.description} label="descrição do rodapé" multiline /></p>
            <a className="mt-4 inline-block text-sm font-semibold text-white hover:text-[var(--accent)]" href={editor ? undefined : `mailto:${sections.footer.email}`}><Editable editor={editor} section="footer" path={["email"]} value={sections.footer.email} label="e-mail do rodapé" /></a>
          </div>
          <div>
            <h2 className="text-sm font-bold uppercase tracking-[0.15em] text-white"><Editable editor={editor} section="footer" path={["servicesTitle"]} value={sections.footer.servicesTitle} label="título das soluções no rodapé" /></h2>
            <ul className="mt-5 space-y-3 text-sm text-slate-400">
              {sections.footer.serviceLinks.map((service, index) => <li key={`${service}-${index}`}><a className="hover:text-white" href={editor ? undefined : "#solucoes"}><Editable editor={editor} section="footer" path={["serviceLinks", index]} value={service} label={`solução ${index + 1} no rodapé`} /></a></li>)}
            </ul>
          </div>
          <div>
            <h2 className="text-sm font-bold uppercase tracking-[0.15em] text-white"><Editable editor={editor} section="footer" path={["institutionalTitle"]} value={sections.footer.institutionalTitle} label="título institucional no rodapé" /></h2>
            <ul className="mt-5 space-y-3 text-sm text-slate-400">
              {sections.navigation.items.map((item, index) => <li key={item.href}><a className="hover:text-white" href={editor ? undefined : item.href}><Editable editor={editor} section="navigation" path={["items", index, "label"]} value={item.label} label={`item ${index + 1} do menu no rodapé`} /></a></li>)}
              {sections.footer.legalLinks.map((link, index) => <li key={link.href}><a className="hover:text-white" href={editor ? undefined : link.href}><Editable editor={editor} section="footer" path={["legalLinks", index, "label"]} value={link.label} label={`link legal ${index + 1}`} /></a></li>)}
            </ul>
          </div>
        </div>
        <div className="border-t border-white/10">
          <div className="site-container flex flex-col gap-2 py-5 text-xs text-slate-500 sm:flex-row sm:items-center sm:justify-between">
            <p>© {currentYear} <Editable editor={editor} section="footer" path={["copyrightName"]} value={sections.footer.copyrightName} label="nome no copyright" />. Todos os direitos reservados.</p>
            <p><Editable editor={editor} section="footer" path={["closingText"]} value={sections.footer.closingText} label="frase final do rodapé" /></p>
          </div>
        </div>
      </footer>
    </div>
  );
}

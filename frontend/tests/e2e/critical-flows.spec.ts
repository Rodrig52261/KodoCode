import { expect, test } from "@playwright/test";

test("hero calls to action scroll without exposing anchors in the URL", async ({ page }) => {
  await page.goto("/");
  const hero = page.locator("#inicio");

  await hero.getByRole("link", { name: "Solicitar orçamento" }).click();
  await expect(page.locator("#contato")).toBeInViewport();
  await expect(page).toHaveURL(/\/$/);

  await hero.getByRole("link", { name: "Conhecer soluções" }).click();
  await expect(page.locator("#solucoes")).toBeInViewport();
  await expect(page).toHaveURL(/\/$/);
});

test("submits a public contact", async ({ page }) => {
  await page.goto("/#contato");
  await page.getByLabel("Nome *").fill("Teste E2E");
  await page.getByLabel("E-mail *").fill(`e2e-${Date.now()}@example.com`);
  await page.getByLabel("Telefone ou WhatsApp *").fill("(11) 99999-9999");
  await page.getByLabel("Serviço de interesse *").selectOption("CRM");
  await page.getByLabel("Faixa de orçamento *").selectOption("DISCUSS_FIRST");
  await page.getByLabel("Mensagem *").fill("Contato automatizado para validar o fluxo completo da aplicação.");
  await page.getByLabel(/Li e aceito/).check();
  await page.waitForTimeout(3_100);
  await page.getByRole("button", { name: "Solicitar orçamento" }).click();
  await expect(page.getByRole("status")).toContainText("Mensagem recebida");
});

test("admin reviews the complete home in preview mode", async ({ page }) => {
  const email = process.env.E2E_ADMIN_EMAIL; const password = process.env.E2E_ADMIN_PASSWORD;
  test.skip(!email || !password, "Set E2E_ADMIN_EMAIL and E2E_ADMIN_PASSWORD");

  await page.goto("/admin/login");
  await page.getByLabel("E-mail").fill(email!);
  await page.getByLabel("Senha").fill(password!);
  await page.getByRole("button", { name: "Entrar" }).click();
  await expect(page).toHaveURL(/admin\/dashboard/);

  await page.goto("/admin/conteudos");
  await expect(page.getByRole("heading", { name: "Edite os textos, revise e publique" })).toBeVisible();
  await expect(page.getByRole("navigation", { name: "Seções editáveis" })).toContainText("Perguntas e respostas");
  await page.getByRole("button", { name: "Visualizar página" }).click();
  const preview = page.getByRole("dialog", { name: "Prévia da página inicial" });
  await expect(preview).toBeVisible();
  await expect(preview.getByRole("heading", { level: 1 })).toBeVisible();
  await preview.getByRole("button", { name: "Voltar ao editor" }).click();
  await expect(preview).toBeHidden();
});

test("admin signs in, publishes text and sees it publicly", async ({ page }) => {
  const email = process.env.E2E_ADMIN_EMAIL; const password = process.env.E2E_ADMIN_PASSWORD;
  test.skip(!email || !password, "Set E2E_ADMIN_EMAIL and E2E_ADMIN_PASSWORD");
  await page.goto("/admin/login"); await page.getByLabel("E-mail").fill(email!); await page.getByLabel("Senha").fill(password!); await page.getByRole("button", { name: "Entrar" }).click();
  await expect(page).toHaveURL(/admin\/dashboard/); await page.goto("/admin/conteudos/hero");
  const title = page.getByLabel("title"); const original = await title.inputValue(); const changed = `${original} · E2E`;
  await title.fill(changed); await page.getByRole("button", { name: "Salvar rascunho" }).click(); await expect(page.getByRole("status")).toContainText("Rascunho salvo");
  page.on("dialog", dialog => dialog.accept()); await page.getByRole("button", { name: "Publicar" }).click(); await expect(page.getByRole("status")).toContainText("publicado");
  await page.goto("/"); await expect(page.getByRole("heading", { level: 1 })).toContainText(changed);
});

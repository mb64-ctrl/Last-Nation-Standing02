# Last Nation Standing — versão candidata ao lançamento (1.0 RC1)

Contém a versão Beta 11 do jogo com zoom de conquista, tela M.B. Studios, menu e TikTok. Projeto Android Kotlin/WebView.

## Como gerar o APK no GitHub
1. Extraia este ZIP no computador. NÃO envie apenas o ZIP ao repositório.
2. No repositório GitHub, envie os arquivos e pastas extraídos preservando a estrutura (incluindo a pasta oculta `.github`). Substitua os arquivos antigos do projeto.
3. Vá a Actions > Gerar APK Android > Run workflow (ou aguarde a execução após commit).
4. Quando o workflow terminar com sucesso, abra a execução e baixe o artefato LastNationStanding-1.0-RC1-APK.
5. Extraia o arquivo do artefato para encontrar app-debug.apk. Instale em um Android de teste.
6. Confira a abertura, mapa, sons, salvamento, TikTok e animações antes de compartilhar publicamente.

## Atenção
- Este projeto não foi compilado nem testado em Android neste ambiente. Pode exigir correções após o primeiro build.
- O APK de debug é apropriado para testes, não para distribuição pública definitiva. Para o lançamento público, gere um APK de release assinado com uma chave privada sua, guardada em segurança.
- A versão web atual usa recursos online (mapa/bandeiras), portanto NÃO é totalmente offline.
- A tela do estúdio também existe no Android nativo; se a versão HTML exibir sua própria tela, podem aparecer duas aberturas consecutivas. Teste e ajuste antes de lançar.

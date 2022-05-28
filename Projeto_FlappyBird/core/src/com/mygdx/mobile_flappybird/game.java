package com.mygdx.mobile_flappybird;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Random;

public class game extends ApplicationAdapter
{
	// guarda a textura
	private SpriteBatch batch;
	private Texture[] passaros;
	private Texture fundo;
	private Texture canoBaixo;
	private Texture canoTopo;
	private Texture gameOver;
	private Texture logo;
	private Texture moeda1;
	private Texture moeda2;
	private Texture moedaAtual;

	// area de colisao dos objetos
	private ShapeRenderer shapeRenderer;
	private Circle circuloPassaro;
	private Rectangle retanguloCanoCima;
	private Rectangle retanguloCanoBaixo;
	private Circle circuloMoeda;

	// config da gameplay, posicionamento de elementos, pontuaçao
	private float larguraDispositivo;
	private float alturaDipositivo;
	private float variacao = 0;
	private float gravidade=2;
	private float posicaoInicialVerticalPassaro=0;
	private float posicaoCanoHorizontal;
	private float posicaoCanoVertical;
	private float espacoEntreCanos;
	private Random random;
	private int pontos=0;
	private int pontuacaoMaxima=0;
	private boolean passouCano=false;
	private int estadoJogo = 0;
	private float posicaoHorizontalPassaro = 50;
	private float escalaPassaro = .5f;
	private float escalaMoeda = .25f;
	private float posicaoMoedaX;
	private float posicaoMoedaY;
	private float valorMoeda1 = 5;
	private float valorMoeda2 = 10;

	// elemetos de texto da inteface 
	private BitmapFont textoPontuacao;
	private BitmapFont textoReiniciar;
	private BitmapFont textoMelhorPontuacao;

	// guarda os sons 
	private Sound somVoando;
	private Sound somColisao;
	private Sound somPontuacao;
	private Sound somMoeda;

	// guarda a pontuaça na memoria do dispositivo
	private Preferences preferencias;

	// camera, e tela
	private OrthographicCamera camera;
	private Viewport viewport;
	private final float VIRTUAL_WIDTH = 720;
	private final float VIRTUAL_HEIGHT = 1280;

	// inicializa objetos, variaveis e assets
	@Override
	public void create()
	{
		inicializarTexturas();
		inicializaObjetos();
	}

	// roda todo o frame
	@Override
	public void render()
	{
		// limpa a tela do jogo
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		// roda a logica do jogo, desenha objetos e inteface do jogo
		verificarEstadoJogo();
		validarPontos();
		desenharCena();
		desenharInterface();
		detectarColisoes();
	}

	// pega referencia dos assets de textura e inicializa o array da animaçao do passaro 
	private void inicializarTexturas()
	{
		passaros = new Texture[3];
		passaros[0] = new Texture("passaro1.png");
		passaros[1] = new Texture("passaro2.png");
		passaros[2] = new Texture("passaro3.png");

		fundo = new Texture("fundo.png");
		logo = new Texture("logo.png");
		canoBaixo = new Texture("cano_baixo_maior.png");
		canoTopo = new Texture("cano_topo_maior.png");
		gameOver = new Texture("game_over.png");
		moeda1 = new Texture("moedaprata.png");
		moeda2 = new Texture("moedaouro.png");

		moedaAtual = moeda2;
	}

	// cria os objetos e da valor inicia as variaveis
	private  void inicializaObjetos()
	{
	        // inicializa classes de utilidade
		batch = new SpriteBatch();
		random = new Random();
                
		// define o valor do tamanho da tela, da valor inicial das posiçoes dos objs
		larguraDispositivo = VIRTUAL_WIDTH;
		alturaDipositivo = VIRTUAL_HEIGHT;
		posicaoInicialVerticalPassaro = alturaDipositivo / 2;
		posicaoCanoHorizontal = larguraDispositivo;
		espacoEntreCanos = 350;
		posicaoMoedaY = alturaDipositivo/2;
		posicaoMoedaX = posicaoCanoHorizontal + larguraDispositivo/2;
                
		// cria o texto que tem a pontuaçao, define cor e tamanho do texto
		textoPontuacao = new BitmapFont();
		textoPontuacao.setColor(Color.WHITE);
		textoPontuacao.getData().setScale(10);
		
                // cria o texto que tem do reset, define cor e tamanho do texto
		textoReiniciar = new BitmapFont();
		textoReiniciar.setColor(Color.GREEN);
		textoReiniciar.getData().setScale(2);
                
		// cria o texto que tem a Melhor Pontuacao, define cor e tamanho do texto
		textoMelhorPontuacao = new BitmapFont();
		textoMelhorPontuacao.setColor(Color.RED);
		textoMelhorPontuacao.getData().setScale(2);
                
		// inicializa os colisores dos objs
		shapeRenderer = new ShapeRenderer();
		circuloPassaro = new Circle();
		retanguloCanoBaixo = new Rectangle();
		retanguloCanoCima = new Rectangle();
		circuloMoeda = new Circle();
                
		// pega referencia dos assets de som
		somVoando = Gdx.audio.newSound(Gdx.files.internal("som_asa.wav"));
		somColisao = Gdx.audio.newSound(Gdx.files.internal("som_batida.wav"));
		somPontuacao = Gdx.audio.newSound(Gdx.files.internal("som_pontos.wav"));
		somMoeda = Gdx.audio.newSound(Gdx.files.internal("som_moeda.wav"));
               
	        // pega preferencias guardadas na memoria e a pontuaçao maxima nas preferencias
		preferencias = Gdx.app.getPreferences("flappyBird");
		pontuacaoMaxima = preferencias.getInteger("pontuacaoMaxima",0);
                
		// inicializa a camera no tamanho da tela
		camera = new OrthographicCamera();
		camera.position.set(VIRTUAL_WIDTH/2, VIRTUAL_HEIGHT/2,0);
		viewport = new StretchViewport(VIRTUAL_WIDTH,VIRTUAL_HEIGHT, camera);
	}

	// roda a logica do jogo dependendo do estado atual
	private  void verificarEstadoJogo()
	{ 
	        // detecta o toque
		boolean toqueTela = Gdx.input.justTouched();
               
	       // antes do jogo começar. se o jogador tocar na tela o jogo inicia
		if (estadoJogo == 0)
		{
			if (toqueTela)
			{
				gravidade = -15;
				estadoJogo = 1;
				somVoando.play();
			}
		}
		// roda a gameplay
		else if (estadoJogo == 1)
		{
			if (toqueTela)
			{
				gravidade = -15;
				somVoando.play();
			}
			
			// move os elementos do jogo
			posicaoCanoHorizontal -= Gdx.graphics.getDeltaTime() * 200;
			posicaoMoedaX -= Gdx.graphics.getDeltaTime() * 200;
			
                        // quandoo cano sai da tela ele reseta a posiçao horizontal e randomiza a posiçao. reseta o valor da variavel "passouCano"
			if (posicaoCanoHorizontal < -canoTopo.getWidth())
			{
				posicaoCanoHorizontal = larguraDispositivo;
				posicaoCanoVertical = random.nextInt(400) - 200;
				passouCano = false;
			}
			
			// quando a moeda sai da tela ele randomiza a posiçao da moeda
			if (posicaoMoedaX < -moedaAtual.getWidth() / 2 * escalaMoeda)
			{
				resetaMoeda();
			}
			
			// quando toca ele pula  e faz a gravidade
			if (posicaoInicialVerticalPassaro > 0 || toqueTela)
			{
				posicaoInicialVerticalPassaro = posicaoInicialVerticalPassaro - gravidade;
				gravidade++;
			}
		}
		
		// tela de gameover
		else if (estadoJogo == 2)
		{
			if (pontos > pontuacaoMaxima)
			{
				pontuacaoMaxima = pontos;
				preferencias.putInteger("pontuacaoMaxima", pontuacaoMaxima);
				preferencias.flush();
			}

			posicaoHorizontalPassaro -= Gdx.graphics.getDeltaTime() * 500;
                        
			// reseta o jogo
			if (toqueTela)
			{
				estadoJogo = 0;
				pontos = 0;
				gravidade = 0;
				posicaoHorizontalPassaro = 50;
				posicaoInicialVerticalPassaro = alturaDipositivo / 2;
				posicaoCanoHorizontal = larguraDispositivo;
				resetaMoeda();
			}
		}
	}

	// gera os colisores e detecta as colisoes
	private void detectarColisoes()
	{
		// Cria e posiciona o colisor do passaro
		circuloPassaro.set
		(
			posicaoHorizontalPassaro + passaros[0].getWidth() * escalaPassaro / 2,
			posicaoInicialVerticalPassaro + passaros[0].getHeight() * escalaPassaro / 2,
			(passaros[0].getHeight()  * escalaPassaro) / 2
		);

		// Cria e posiciona o colisor da moeda
		circuloMoeda.set
		(
			posicaoMoedaX - ((moedaAtual.getWidth() * escalaMoeda) / 2),
			posicaoMoedaY - ((moedaAtual.getHeight() * escalaMoeda) / 2),
			(moedaAtual.getWidth() * escalaMoeda) / 2
		);
		
		// cria e posiciona o colisor dos canos
		retanguloCanoBaixo.set
				(
						posicaoCanoHorizontal,
						alturaDipositivo / 2 - canoBaixo.getHeight() - espacoEntreCanos / 2 + posicaoCanoVertical,
						canoBaixo.getWidth(),
						canoBaixo.getHeight()
				);

		retanguloCanoCima.set
				(
						posicaoCanoHorizontal,
						alturaDipositivo /2 + espacoEntreCanos /2 + posicaoCanoVertical,
						canoTopo.getWidth(),
						canoTopo.getHeight()
				);
				
		// detecta as colisoes 
		boolean colidiuCanoCima  = Intersector.overlaps(circuloPassaro, retanguloCanoCima);
		boolean colidiuCanoBaixo = Intersector.overlaps(circuloPassaro, retanguloCanoBaixo);
		boolean colidiuMoeda 	 = Intersector.overlaps(circuloPassaro, circuloMoeda);
		
		// colidiu com a moeda adiciona pontuaçao, posiciona a moeda para fora da tela e toca o som de coletar moeda
		if(colidiuMoeda == true)
		{
			if(moedaAtual == moeda1) pontos += valorMoeda1;
			else pontos += valorMoeda2;

			posicaoMoedaY = alturaDipositivo * 2;
			somMoeda.play();
		}
		
		// colidiu com um dos canos, toca o som de colisao e morre
		if (colidiuCanoBaixo || colidiuCanoCima)
		{
			if (estadoJogo == 1)
			{
				somColisao.play();
				estadoJogo = 2;
			}
		}
	}

	// desenha sprites do jogo
	private void desenharCena()
	{
	        // traduz coordenadas do mundo pra coordenadas da tela
		batch.setProjectionMatrix(camera.combined);
		
		// permite q voce desenhe objs na tela
		batch.begin();
		
		batch.draw(fundo,0,0,larguraDispositivo,alturaDipositivo);
		batch.draw
		(
			passaros[(int) variacao],
			posicaoHorizontalPassaro,
			posicaoInicialVerticalPassaro,
			passaros[0].getWidth() * escalaPassaro,
			passaros[0].getHeight() * escalaPassaro
		);

		if(estadoJogo != 0)
		{
			// Desenha Cano baixo
			batch.draw
			(
				canoBaixo,
				posicaoCanoHorizontal,
				alturaDipositivo / 2 - canoBaixo.getHeight() - espacoEntreCanos/2 + posicaoCanoVertical
			);

			// Desenha cano topo
			batch.draw
			(
				canoTopo,
				posicaoCanoHorizontal,
				alturaDipositivo / 2 + espacoEntreCanos / 2 + posicaoCanoVertical
			);

			// Desenha moeda
			batch.draw
			(
				moedaAtual,
				posicaoMoedaX - (moedaAtual.getWidth() * escalaMoeda),
				posicaoMoedaY - (moedaAtual.getWidth() * escalaMoeda),
				moedaAtual.getWidth() * escalaMoeda,
				moedaAtual.getHeight() * escalaMoeda
			);
		}
		
		batch.end();
	}

	// desenha o que tem na interface
	private void desenharInterface()
	{
		batch.setProjectionMatrix(camera.combined);
		batch.begin();

		// Desenha pontuação
		textoPontuacao.draw
		(
			batch,
			String.valueOf(pontos),
			larguraDispositivo/ 2,
			alturaDipositivo - 110
		);
		
		// desenha os elementos da interface dependendo do estado atual
		switch (estadoJogo)
		{
			case 0:
			{
				// Desenha logo
				batch.draw
				(
					logo,
					larguraDispositivo/2 - logo.getWidth()/2/4,
					alturaDipositivo / 3,
					logo.getWidth()/4,
					logo.getHeight()/4
				);
			}break;

			case 1: {} break;

			case 2:
			{
				// Desenha imagem gameover
				batch.draw
				(
					gameOver,
					larguraDispositivo/2 - gameOver.getWidth()/2,
					alturaDipositivo / 2
				);

				// Desenha texto reiniciar
				textoReiniciar.draw
				(
					batch,
					"Toque para reiniciar!",
					larguraDispositivo/2 -140,
					alturaDipositivo/2 - gameOver.getHeight()/2
				);

				// Desenha high score
				textoMelhorPontuacao.draw
				(
					batch,
					"Seu Record é: " + pontuacaoMaxima + " pontos",
					larguraDispositivo/2 -140,
					alturaDipositivo/2 - gameOver.getHeight()
				);
			}break;
		}
		batch.end();
	}

	// da pontuaçao ao jogador se ele passou pelos canos e roda a animaçao do player
	public void validarPontos()
	{
		if (posicaoCanoHorizontal < posicaoHorizontalPassaro)
		{
			if(!passouCano)
			{
				pontos++;
				passouCano = true;
				somPontuacao.play();
			}
		}

		variacao += Gdx.graphics.getDeltaTime() * 10;

		if (variacao > 3)
		{
			variacao = 0;
		}
	}

	// randomiza a posiçao da moeda entre os proximos canos e randomiza o tipo da moeda
	private void resetaMoeda()
	{
		posicaoMoedaX = posicaoCanoHorizontal + canoBaixo.getWidth() + moedaAtual.getWidth() + random.nextInt((int) (larguraDispositivo - (moedaAtual.getWidth() * escalaMoeda)));
		posicaoMoedaY = moedaAtual.getHeight() / 2 + random.nextInt((int) alturaDipositivo - moedaAtual.getHeight() / 2);

		int randomMoedaNova = random.nextInt(100);
		if(randomMoedaNova < 30)
		{
			moedaAtual = moeda2;
		}
		else
		{
			moedaAtual = moeda1;
		}
	}

	// redimenciona o tamanho da tela do jogo
	@Override
	public void resize(int width, int height)
	{
		viewport.update(width, height);
	}

	// memory management
	@Override
	public void dispose () {}
}

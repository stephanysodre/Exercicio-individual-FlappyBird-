package com.mygdx.game;

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
import com.badlogic.gdx.utils.Logger;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.sun.tools.javac.util.Log;

import java.util.Random;

public class jogo extends ApplicationAdapter {
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
	float escalamoeda = .25f;
	float posicaoMoedaX;
	float posicaoMoedaY;
	float valorMoeda1 = 5;
	float valormoeda2 = 10;

	private ShapeRenderer shapeRenderer;
	private Circle circuloPassaro;
	private Rectangle retanguloCanoCima;
	private Rectangle retanguloCanoBaixo;
	private Circle circuloMoeda;

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
	private float posicaoHorizontalPassaro = 0;

	BitmapFont textoPontuacao;
	BitmapFont textoReiniciar;
	BitmapFont textoMelhorPontuacao;

	Sound somVoando;
	Sound somColisao;
	Sound somPontuacao;

	Preferences preferencias;

	private OrthographicCamera camera;
	private Viewport viewport;
	private final float VIRTUAL_WIDTH = 720;
	private final float VIRTUAL_HEIGHT = 1280;

	@Override
	public void create()
	{
		inicializarTexturas();
		inicializaObjetos();
	}

	@Override
	public void render()
	{
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		verificarEstadoJogo();
		validarPontos();
		desenharTexturas();
		detectarColisoes();
	}



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
		moeda1 = new Texture("moedaouro.png");
		moeda2 = new Texture("moedaprata.png");

		moedaAtual = moeda2;
	}

	private  void inicializaObjetos()
	{
		batch = new SpriteBatch();
		random = new Random();

		larguraDispositivo = VIRTUAL_WIDTH;
		alturaDipositivo = VIRTUAL_HEIGHT;
		posicaoInicialVerticalPassaro = alturaDipositivo / 2;
		posicaoCanoHorizontal = larguraDispositivo;
		espacoEntreCanos = 350;
		posicaoMoedaY = alturaDipositivo/2;
		posicaoMoedaX = posicaoCanoHorizontal + larguraDispositivo/2;

		textoPontuacao = new BitmapFont();
		textoPontuacao.setColor(Color.WHITE);
		textoPontuacao.getData().setScale(10);

		textoReiniciar = new BitmapFont();
		textoReiniciar.setColor(Color.GREEN);
		textoReiniciar.getData().setScale(2);

		textoMelhorPontuacao = new BitmapFont();
		textoMelhorPontuacao.setColor(Color.RED);
		textoMelhorPontuacao.getData().setScale(2);

		shapeRenderer = new ShapeRenderer();
		circuloPassaro = new Circle();
		retanguloCanoBaixo = new Rectangle();
		retanguloCanoCima = new Rectangle();
		circuloMoeda = new Circle();

		somVoando = Gdx.audio.newSound(Gdx.files.internal("som_asa.wav"));
		somColisao = Gdx.audio.newSound(Gdx.files.internal("som_batida.wav"));
		somPontuacao = Gdx.audio.newSound(Gdx.files.internal("som_pontos.wav"));

		preferencias = Gdx.app.getPreferences("flappyBird");
		pontuacaoMaxima = preferencias.getInteger("pontuacaoMaxima",0);

		camera = new OrthographicCamera();
		camera.position.set(VIRTUAL_WIDTH/2, VIRTUAL_HEIGHT/2,0);
		viewport = new StretchViewport(VIRTUAL_WIDTH,VIRTUAL_HEIGHT, camera);
	}

	private  void verificarEstadoJogo()
	{
		boolean toqueTela = Gdx.input.justTouched();

		if (estadoJogo == 0)
		{
			if (toqueTela)
			{
				gravidade = -15;
				estadoJogo = 1;
				somVoando.play();
			}
		}
		else if (estadoJogo == 1)
		{
			if (toqueTela)
			{
				gravidade = -15;
				somVoando.play();
			}

			posicaoCanoHorizontal -= Gdx.graphics.getDeltaTime() * 200;
			posicaoMoedaX -= Gdx.graphics.getDeltaTime() * 200;

			if (posicaoCanoHorizontal < -canoTopo.getWidth())
			{
				posicaoCanoHorizontal = larguraDispositivo;
				posicaoCanoVertical = random.nextInt(400) - 200;
				passouCano = false;
			}
			if (posicaoInicialVerticalPassaro > 0 || toqueTela)
			{
				posicaoInicialVerticalPassaro = posicaoInicialVerticalPassaro - gravidade;
				gravidade++;
			}
		}
		else if (estadoJogo == 2)
		{
			if (pontos > pontuacaoMaxima)
			{
				pontuacaoMaxima = pontos;
				preferencias.putInteger("pontuacaoMaxima", pontuacaoMaxima);
				preferencias.flush();
			}

			posicaoHorizontalPassaro -= Gdx.graphics.getDeltaTime() * 500;

			if (toqueTela)
			{
				estadoJogo = 0;
				pontos = 0;
				gravidade = 0;
				posicaoHorizontalPassaro = 0;
				posicaoInicialVerticalPassaro = alturaDipositivo / 2;
				posicaoCanoHorizontal = larguraDispositivo;
			}
		}
	}

	private void detectarColisoes()
	{
		// Cria colisor passaro
		circuloPassaro.set
		(
			50 + posicaoHorizontalPassaro + passaros[0].getWidth() / 2 / 2,
			posicaoInicialVerticalPassaro + passaros[0].getHeight() / 2 / 2,
			(passaros[0].getWidth() / 2) / 2
		);

		// Cria colisor moeda
		circuloMoeda.set
		(
			posicaoMoedaX - moedaAtual.getWidth()/2 * escalamoeda,
			posicaoMoedaY - moedaAtual.getHeight()/2 * escalamoeda,
			(moedaAtual.getWidth() * escalamoeda) / 2
		);

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

		boolean colidiuCanoCima  = Intersector.overlaps(circuloPassaro, retanguloCanoCima);
		boolean colidiuCanoBaixo = Intersector.overlaps(circuloPassaro, retanguloCanoBaixo);
		boolean colidiuMoeda 	 = Intersector.overlaps(circuloPassaro, circuloMoeda);

		if(colidiuMoeda == true)
		{
			pontos += valorMoeda1;
			posicaoMoedaY = alturaDipositivo * 2;
		}

		if (colidiuCanoBaixo || colidiuCanoCima)
		{
			if (estadoJogo == 1)
			{
				somColisao.play();
				estadoJogo = 2;
			}
		}
	}

	private void desenharTexturas()
	{
		batch.setProjectionMatrix(camera.combined);
		batch.begin();

		batch.draw(fundo,0,0,larguraDispositivo,alturaDipositivo);

		batch.draw
		(
			passaros[(int) variacao],
			50 + posicaoHorizontalPassaro,
			posicaoInicialVerticalPassaro,
			passaros[(int) variacao].getWidth()/2,
			passaros[(int) variacao].getHeight()/2
		);

		textoPontuacao.draw
		(
			batch,
			String.valueOf(pontos),
			larguraDispositivo/ 2,
			alturaDipositivo - 110
		);

		if(estadoJogo == 0 )
		{
			batch.draw
			(
				logo,
				larguraDispositivo/2 - logo.getWidth()/2/4,
				alturaDipositivo / 3,
				logo.getWidth()/4,
				logo.getHeight()/4
			);
		}

		if(estadoJogo == 1)
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
				posicaoMoedaX - moedaAtual.getWidth() / 2 * escalamoeda,
				posicaoMoedaY - moedaAtual.getWidth() / 2 * escalamoeda,
				moedaAtual.getWidth() * escalamoeda,
				moedaAtual.getHeight() * escalamoeda
			);
		}
		if(estadoJogo == 2 )
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

			// Desenha cano baixo
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
		}

		batch.end();
	}

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

	@Override
	public void resize(int width, int height)
	{
		viewport.update(width, height);
	}

	@Override
	public void dispose () {}
}

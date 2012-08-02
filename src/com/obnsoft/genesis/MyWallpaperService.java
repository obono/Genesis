package com.obnsoft.genesis;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLUtils;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.SurfaceHolder;

public class MyWallpaperService extends WallpaperService {

    @Override
    public Engine onCreateEngine( )
    {
        return new GLEngine( );
    }

    public void log( String msg )
    {
        Log.d( "GLES_SAMPLE", msg );
    }

    public class GLEngineRender
    {

        int vertexBufferObject = 0;

        int textureID          = 0;
        int textureHeight      = 0;
        int textureWidth       = 0;

        int windowWidth        = 0;
        int windowHeight       = 0;

        float degree = 0;

        public int getWindowWidth( )
        {
            return windowWidth;
        }

        public int getWindowHeight( )
        {
            return windowHeight;
        }

        public void onSurfaceChanged( GL10 gl10, int width, int height )
        {
            windowWidth = width;
            windowHeight = height;

            // ! ���O�ɓ]���ς݂ɂ��Ă���
            {
                float vertices[] =
                {
                        // !  x       y    z     u       v
                        -0.5f,
                        0.5f,
                        0.0f,
                        0.0f,
                        0.0f, // !< ����
                        -0.5f,
                        -0.5f,
                        0.0f,
                        0.0f,
                        1.0f, // !< ����
                        0.5f,
                        0.5f,
                        0.0f,
                        1.0f,
                        0.0f, // !< �E��
                        0.5f,
                        -0.5f,
                        0.0f,
                        1.0f,
                        1.0f, // !< �E��
                };

                ByteBuffer bb = ByteBuffer.allocateDirect( vertices.length * 4 );
                bb.order( ByteOrder.nativeOrder( ) );
                FloatBuffer vbo = bb.asFloatBuffer( );
                vbo.put( vertices );
                vbo.position( 0 );

                gl10.glEnableClientState( GL10.GL_TEXTURE_COORD_ARRAY );
                gl10.glEnableClientState( GL10.GL_VERTEX_ARRAY );

                GL11 gl11 = ( GL11 ) gl10;
                //! ���_�I�u�W�F�N�g�쐬
                {
                    int[] buffers = new int[ 1 ];
                    gl11.glGenBuffers( 1, buffers, 0 );
                    vertexBufferObject = buffers[ 0 ];
                    gl11.glBindBuffer( GL11.GL_ARRAY_BUFFER, vertexBufferObject );
                    gl11.glBufferData( GL11.GL_ARRAY_BUFFER, vbo.capacity( ) * 4, vbo, GL11.GL_STATIC_DRAW );
                }
                {
                    gl11.glVertexPointer( 3, GL10.GL_FLOAT, 4 * 5, 0 );
                    gl11.glTexCoordPointer( 2, GL10.GL_FLOAT, 4 * 5, 4 * 3 );
                }
                gl11.glBindBuffer( GL11.GL_ARRAY_BUFFER, 0 );
            }
            //! texture
            /*
            */
            {
                Bitmap bitmap = BitmapFactory.decodeResource( getResources( ), R.drawable.image_512 );

                gl10.glEnable( GL10.GL_TEXTURE_2D );
                int[] buffer = new int[ 1 ];
                gl10.glGenTextures( 1, buffer, 0 );
                textureID = buffer[ 0 ];
                textureWidth = bitmap.getWidth( );
                textureHeight = bitmap.getHeight( );

                gl10.glBindTexture( GL10.GL_TEXTURE_2D, textureID );
                GLUtils.texImage2D( GL10.GL_TEXTURE_2D, 0, bitmap, 0 );
                gl10.glTexParameterf( GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST );
                gl10.glTexParameterf( GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_NEAREST );

                // ! bitmap��j��
                bitmap.recycle( );
            }
        }

        /**
         * �l�p�`��`�悷��B
         *
         * @param gl10
         * @param x
         * @param y
         * @param w
         * @param h
         */
        public void drawQuad( GL10 gl10, int x, int y, int w, int h )
        {
            // ! �`��ʒu���s��ő��삷��
            float sizeX = ( float ) w / ( float ) getWindowWidth( ) * 2;
            float sizeY = ( float ) h / ( float ) getWindowHeight( ) * 2;
            float sx = ( float ) x / ( float ) getWindowWidth( ) * 2;
            float sy = ( float ) y / ( float ) getWindowHeight( ) * 2;

            gl10.glLoadIdentity( );
            degree += 1f;
            gl10.glRotatef(degree, 0.0f, 0.0f, 1.0f);
            gl10.glTranslatef( -1.0f + sizeX / 2.0f + sx, 1.0f - sizeY / 2.0f - sy, 0.0f );
            gl10.glScalef( sizeX, sizeY, 1.0f );
            gl10.glDrawArrays( GL10.GL_TRIANGLE_STRIP, 0, 4 );
        }

        /**
         *
         * @param gl10
         * @param x
         * @param y
         * @param w
         * @param h
         */
        private void setTextureArea( GL10 gl10, int x, int y, int w, int h )
        {
            float tw = ( float ) w / ( float ) textureWidth;
            float th = ( float ) h / ( float ) textureHeight;
            float tx = ( float ) x / ( float ) textureWidth;
            float ty = ( float ) y / ( float ) textureHeight;

            gl10.glMatrixMode( GL10.GL_TEXTURE );
            gl10.glLoadIdentity( );
            gl10.glTranslatef( tx, ty, 0.0f );
            gl10.glScalef( tw, th, 1.0f );
            gl10.glMatrixMode( GL10.GL_MODELVIEW );
        }

        public void onDrawFrame( GL10 gl10 )
        {
            gl10.glClearColor( 0.5f, 0.5f, 0.5f, 1 );
            gl10.glClear( GL10.GL_COLOR_BUFFER_BIT );

            setTextureArea( gl10, 0, 0, 512, 512 );
            drawQuad( gl10, 0, 0, 256, 256 );
        }
    }

    public class GLEngineSurface extends Thread
    {
        public GLEngineSurface( SurfaceHolder holder )
        {
            this.holder = holder;
        }

        private boolean       destroy    = false;
        private boolean       pause      = false;

        /**
         * �`��Ώۂ�holder�B
         */
        private SurfaceHolder holder;

        /**
         * EGL�C���^�[�t�F�[�X�B
         */
        private EGL10         egl;

        /**
         * GL�R���e�L�X�g�B
         */
        private EGLContext    eglContext = null;
        /**
         * �f�B�X�v���C�B
         */
        private EGLDisplay    eglDisplay = null;
        /**
         * �T�[�t�F�C�X�B
         */
        private EGLSurface    eglSurface = null;

        /**
         * �R���t�B�O���B
         */
        private EGLConfig     eglConfig  = null;

        /**
         * GL�p�C���^�[�t�F�[�X�B
         */
        protected GL10        gl10       = null;

        /**
         * �`���T�[�t�F�C�X�̕��E����
         */
        private int           windowWidth = -1, windowHeight = -1;

        /**
         * GL�̊J�n�������s���B
         */
        private void initialize( )
        {
            egl = ( EGL10 ) EGLContext.getEGL( );

            //! �`���f�B�X�v���C�m��
            eglDisplay = egl.eglGetDisplay( EGL10.EGL_DEFAULT_DISPLAY );

            //! EGL�������B
            //! ������GLES�̃o�[�W�������擾�ł��邪�AES1.0���K���A���Ă���悤�ł���B
            {
                int[] version =
                {
                        -1, -1
                };
                if( !egl.eglInitialize( eglDisplay, version ) )
                {
                    log( "!eglInitialize" );
                    return;
                }
            }

            //! �R���t�B�O�擾
            {
                EGLConfig[] configs = new EGLConfig[ 1 ];
                int[] num = new int[ 1 ];

                //! ���̔z���GL�̐��\���w�肷��B
                //! �f�B�X�v���C�̐F�[�x�AZ�[�x�������Ŏw�肷�邪�A
                //! ��{�I��2D�`�悷��ꍇ�̓f�t�H���g�̂܂܂ł����ɖ��Ȃ��B
                //! spec�ɑΉ����Ă��Ȃ��l�����Ă������������s����B
                int[] spec =
                {
                    EGL10.EGL_NONE
                //! �I�[�ɂ�EGL_NONE������
                };
                if( !egl.eglChooseConfig( eglDisplay, spec, configs, 1, num ) )
                {
                    log( "!eglChooseConfig" );
                    return;
                }

                eglConfig = configs[ 0 ];
            }

            //! �����_�����O�R���e�L�X�g�쐬
            {
                //�����_�����O�R���e�L�X�g�쐬
                eglContext = egl.eglCreateContext( eglDisplay, eglConfig, EGL10.EGL_NO_CONTEXT, null );
                if( eglContext == EGL10.EGL_NO_CONTEXT )
                {
                    log( "glContext == EGL10.EGL_NO_CONTEXT" );
                    return;
                }
            }
            //! �`���T�[�t�F�C�X���쐬����
            {
                //! SurfaceHolder�Ɍ��т���
                egl.eglMakeCurrent( eglDisplay, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT );
                eglSurface = egl.eglCreateWindowSurface( eglDisplay, eglConfig, holder, null );

                if( eglSurface == EGL10.EGL_NO_SURFACE )
                {
                    log( "glSurface == EGL10.EGL_NO_SURFACE" );
                    return;
                }
            }

            //! GLES�C���^�[�t�F�[�X�擾
            {
                gl10 = ( GL10 ) eglContext.getGL( );
            }

            //! �T�[�t�F�C�X�ƃR���e�L�X�g�����т���
            {
                if( !egl.eglMakeCurrent( eglDisplay, eglSurface, eglSurface, eglContext ) )
                {
                    log( "!eglMakeCurrent" );
                    return;
                }
            }
        }

        /**
         * GL�̏I���������s���B
         */
        private void dispose( )
        {
            //�T�[�t�F�C�X�j��
            if( eglSurface != null )
            {
                //�����_�����O�R���e�L�X�g�Ƃ̌��т��͉���

                /**
                 * �f�t�H���g�ɖ߂���������Ȃ��ƕ����ł��Ȃ�
                 */
                egl.eglMakeCurrent( eglDisplay, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT );
                egl.eglDestroySurface( eglDisplay, eglSurface );
                eglSurface = null;
            }

            //�����_�����O�R���e�L�X�g�j��
            if( eglContext != null )
            {
                egl.eglDestroyContext( eglDisplay, eglContext );
                eglContext = null;
            }

            //�f�B�X�v���C�R�l�N�V�����j��
            if( eglDisplay != null )
            {
                egl.eglTerminate( eglDisplay );
                eglDisplay = null;
            }
        }

        @Override
        public void run( )
        {
            GLEngineRender render = new GLEngineRender( );
            initialize( );
            render.onSurfaceChanged( gl10, windowWidth, windowHeight );
            while ( !destroy )
            {
                if( !pause )
                {
                    render.windowHeight = windowHeight;
                    render.windowWidth = windowWidth;
                    gl10.glViewport( 0, 0, windowWidth, windowHeight );
                    render.onDrawFrame( gl10 );
                    egl.eglSwapBuffers( eglDisplay, eglSurface );
                }
                else
                {
                    try
                    {
                        Thread.sleep( 100 );
                    }
                    catch( Exception e )
                    {

                    }
                }
            }
            dispose( );
        }

        public void onPause( )
        {
            pause = true;
        }

        public void onResume( )
        {
            pause = false;
        }

        /**
         * �X���b�h���~�߂ď������I������B
         */
        public void onDestroy( )
        {
            synchronized( this )
            {
                //�I���v�����o��
                destroy = true;
            }

            try
            {
                //�X���b�h�I����҂�
                join( );
            }
            catch( InterruptedException ex )
            {
                Thread.currentThread( ).interrupt( );
            }
        }

    }

    /**
     *
     */
    public class GLEngine extends Engine
    {
        private GLEngineSurface gl = null;

        /**
         * �T�[�t�F�C�X���쐬���ꂽ�B
         *
         * @param surfaceHolder
         */
        @Override
        public void onCreate( SurfaceHolder surfaceHolder )
        {
            super.onCreate( surfaceHolder );
            surfaceHolder.setType( SurfaceHolder.SURFACE_TYPE_GPU );

        }

        /**
         * ���E�s�����ύX���ꂽ�B
         *
         * @param visible
         */
        @Override
        public void onVisibilityChanged( boolean visible )
        {
            log( "onVisibilityChanged : " + visible );
            super.onVisibilityChanged( visible );

            if( visible )
            {
                gl.onResume( );
            }
            else
            {
                gl.onPause( );
            }
        }

        @Override
        public void onSurfaceCreated( SurfaceHolder holder )
        {
            super.onSurfaceCreated( holder );
            gl = new GLEngineSurface( getSurfaceHolder( ) );
            gl.start( );
        }

        @Override
        public void onSurfaceChanged( SurfaceHolder holder, int format, int width, int height )
        {
            log( "onSurfaceChanged" );
            log( "" + width + " x " + height );
            super.onSurfaceChanged( holder, format, width, height );

            gl.windowWidth = width;
            gl.windowHeight = height;
        }

        @Override
        public void onSurfaceDestroyed( SurfaceHolder holder )
        {
            log( "onSurfaceDestroyed" );
            super.onSurfaceDestroyed( holder );

            if( gl != null )
            {
                gl.onDestroy( );
                gl = null;
            }
        }
    }

}

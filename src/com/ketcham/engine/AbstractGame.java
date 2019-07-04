package com.ketcham.engine;

public abstract class AbstractGame
{
    public abstract void update(GameContainer gc);

    public abstract void render(GameContainer gc, Renderer r);
}
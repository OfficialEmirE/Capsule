package net.capsule.gui;

import java.util.concurrent.ThreadLocalRandom;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.gui.screen.IBackground;
import me.ramazanenescik04.diken.resource.Bitmap;

public class RandomPositionBg implements IBackground {
    private final Bitmap sprite;
    private final DikenEngine engine;
    
    // Konumlar
    private float offsetX, offsetY;
    // Mevcut anlık hız
    private float curSpeedX, curSpeedY;
    // Ulaşılmak istenen hedef hız
    private float targetSpeedX, targetSpeedY;
    
    // Ayarlar (Bu değerlerle oynayarak yumuşaklığı değiştirebilirsin)
    private final float acceleration = 0.05f; // Hızlanma/Yavaşlama hassasiyeti
    private final float maxSpeed = 3.0f;     // Maksimum hız

    public RandomPositionBg(Bitmap gamesPanelBg) {
        this.sprite = gamesPanelBg;
        this.engine = DikenEngine.getEngine();
        pickNewTargetSpeed();
    }

    private void pickNewTargetSpeed() {
        var rand = ThreadLocalRandom.current();
        // Hedef hızı rastgele belirle (-maxSpeed ile +maxSpeed arası)
        this.targetSpeedX = (rand.nextFloat() * maxSpeed * 2) - maxSpeed;
        this.targetSpeedY = (rand.nextFloat() * maxSpeed * 2) - maxSpeed;
    }

    public void tick() {
        // --- YUMUŞAK GEÇİŞ MANTIĞI ---
        // Mevcut hızı, hedef hıza doğru yavaşça yaklaştır (Lerp mantığı)
        curSpeedX += (targetSpeedX - curSpeedX) * acceleration;
        curSpeedY += (targetSpeedY - curSpeedY) * acceleration;

        // Pozisyonu güncelle
        offsetX += curSpeedX;
        offsetY += curSpeedY;

        // Sonsuz döngü (Tile kaydırma)
        offsetX %= sprite.w;
        offsetY %= sprite.h;

        // Rastgele zamanlarda hedef hızı değiştir (Daha doğal bir his için)
        if (ThreadLocalRandom.current().nextInt(200) < 1) {
            pickNewTargetSpeed();
        }
    }

    @Override
    public void render(Bitmap canvas) {
        // Ekranı kaplayacak kadar tile çiz (Sadece gerekli alanı tarar)
        int tilesX = (engine.getWidth() / sprite.w) + 2;
        int tilesY = (engine.getHeight() / sprite.h) + 2;

        for (int i = -1; i < tilesX; i++) {
            for (int j = -1; j < tilesY; j++) {
                int drawX = (int) (i * sprite.w + offsetX);
                int drawY = (int) (j * sprite.h + offsetY);
                canvas.blendDraw(this.sprite, drawX, drawY, 0xff7d7d7d);
            }
        }
    }
}
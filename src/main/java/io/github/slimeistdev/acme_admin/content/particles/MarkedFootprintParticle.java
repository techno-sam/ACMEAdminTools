/*
 * ACME Admin Tools
 * Copyright (c) 2024 Sam Wagenaar and VivvyInks
 *
 * FootprintParticle (https://github.com/Rivmun/FootprintParticle)
 * Copyright (c) 2023 Rivmun
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.slimeistdev.acme_admin.content.particles;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import io.github.slimeistdev.acme_admin.mixin_ducks.client.AbstractClientPlayer_Duck;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@Environment(EnvType.CLIENT)
public class MarkedFootprintParticle extends TextureSheetParticle {

    public static final ParticleRenderType PARTICLE_SHEET_TRANSLUCENT_NO_DEPTH_TEST = new ParticleRenderType() {
        @Override
        @SuppressWarnings("deprecation")
        public void begin(BufferBuilder builder, TextureManager textureManager) {
            RenderSystem.depthMask(true);
            RenderSystem.disableDepthTest();
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        }

        @Override
        public void end(Tesselator tesselator) {
            tesselator.end();
            RenderSystem.enableDepthTest();
        }

        public String toString() {
            return "PARTICLE_SHEET_TRANSLUCENT_NO_DEPTH_TEST";
        }
    };

    private final MarkedExistenceTracker existenceTracker;
    private final int fadeTicks;
    private final float angle;

    protected MarkedFootprintParticle(ClientLevel level, double x, double y, double z, MarkedExistenceTracker existenceTracker, int survivalTicks, float angle) {
        super(level, x, y, z);

        this.existenceTracker = existenceTracker;
        setLifetime(survivalTicks);
        this.fadeTicks = survivalTicks / 2;
        this.angle = angle;

        this.quadSize = 0.15625f;

        this.rCol = 0.1f;
        this.gCol = 1.0f;
        this.bCol = 0.05f;
    }

    @Override
    protected int getLightColor(float partialTick) {
        return LightTexture.FULL_BRIGHT; // glow!
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return PARTICLE_SHEET_TRANSLUCENT_NO_DEPTH_TEST;
    }

    @Override
    public void tick() {
        super.tick();

        this.y -= 0.1f / lifetime;

        if (!existenceTracker.isActive()) {
            age /= 2;
            lifetime /= 2;
        }

        if (age >= lifetime - fadeTicks) {
            setAlpha((lifetime - age) / (float) fadeTicks);

            float factor = Mth.clamp((alpha * 2f) - 1, 0, 1);
            this.rCol = Mth.lerp(factor, 1.0f, 0.1f);
            this.gCol = Mth.lerp(factor, 0.1f, 1.0f);
        }
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTicks) {
        Vec3 camPos = camera.getPosition();
        float x = (float)(Mth.lerp(partialTicks, this.xo, this.x) - camPos.x());
        float y = (float)(Mth.lerp(partialTicks, this.yo, this.y) - camPos.y());
        float z = (float)(Mth.lerp(partialTicks, this.zo, this.z) - camPos.z());
        /*
         * In Minecraft,
         * rotate a vec3f point P around an axis with θ radian (anticlockwise)
         * needs a NORMALIZED normal vec3f(x, y, z) direct to this axis direction
         * then simply call P.rotate(new Quaternion(sinθ*x, sinθ*y, sinθ*z, cosθ)) with half the θ
         * Oh, MAGIC! (👈 he is totally idiot.)1
         *
         * here we need vertex rotate around Y axis, so the normal is (0, 1, 0), that's let X and Z are 0.
         */
        Quaternionf q = new Quaternionf(
            0,
            Mth.sin(this.angle / 2),
            0,
            Mth.cos(this.angle / 2)
        );
        Vector3f[] pos = new Vector3f[]{new Vector3f(-1, 0, -1), new Vector3f(-1, 0, 1), new Vector3f(1, 0, 1), new Vector3f(1, 0, -1)};
        float i = this.getQuadSize(partialTicks);

        for (int j = 0; j < 4; ++j) {
            Vector3f vec3f = pos[j];
            vec3f.rotate(q);
            vec3f.mul(i);
            vec3f.add(x, y, z);
        }

        float k = this.getU0();
        float l = this.getU1();
        float n = this.getV1();
        float m = this.getV0();
        int o = this.getLightColor(partialTicks);
        buffer.vertex(pos[0].x(), pos[0].y(), pos[0].z()).uv(l, n).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(o).endVertex();
        buffer.vertex(pos[1].x(), pos[1].y(), pos[1].z()).uv(l, m).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(o).endVertex();
        buffer.vertex(pos[2].x(), pos[2].y(), pos[2].z()).uv(k, m).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(o).endVertex();
        buffer.vertex(pos[3].x(), pos[3].y(), pos[3].z()).uv(k, n).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(o).endVertex();
    }

    public static class Provider implements ParticleProvider<MarkedFootprintParticleOptions> {
        private final SpriteSet sprite;

        public Provider(SpriteSet sprite) {
            this.sprite = sprite;
        }

        @Nullable
        @Override
        public Particle createParticle(MarkedFootprintParticleOptions options, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            if (!(level.getEntity(options.ownerId()) instanceof AbstractClientPlayer_Duck player)) return null;

            MarkedExistenceTracker tracker = player.acme_admin$getMarkedExistenceTracker();
            if (tracker == null) return null;

            MarkedFootprintParticle particle = new MarkedFootprintParticle(
                level, x, y, z,
                tracker, options.survivalSeconds() * 20,
                (float) Math.atan2(zSpeed, xSpeed)
            );
            particle.setAlpha(1.0f);
            particle.pickSprite(this.sprite);
            return particle;
        }
    }
}

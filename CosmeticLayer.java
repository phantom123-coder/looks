package com.looksmaxxing.client;

import com.looksmaxxing.Looksmaxxing;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;

/** Extra geometry on the head: hair (colored) and face features (use the player's own skin texture). */
public class CosmeticLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    public static final ModelLayerLocation LOCATION =
            new ModelLayerLocation(new ResourceLocation(Looksmaxxing.MODID, "cosmetics"), "main");
    private static final ResourceLocation WHITE =
            new ResourceLocation(Looksmaxxing.MODID, "textures/entity/white.png");

    private final ModelPart[] hair = new ModelPart[6];
    private final ModelPart nose, chin, jawL, jawR, cheekL, cheekR;

    public CosmeticLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent, ModelPart root) {
        super(parent);
        for (int i = 0; i < hair.length; i++) hair[i] = root.getChild("hair" + i);
        nose = root.getChild("nose");
        chin = root.getChild("chin");
        jawL = root.getChild("jawL");
        jawR = root.getChild("jawR");
        cheekL = root.getChild("cheekL");
        cheekR = root.getChild("cheekR");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        CubeDeformation d6 = new CubeDeformation(0.6f);
        CubeDeformation d3 = new CubeDeformation(0.3f);
        CubeDeformation d2 = new CubeDeformation(0.2f);

        root.addOrReplaceChild("hair0", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-4f, -8f, -4f, 8f, 1f, 8f, d6), PartPose.ZERO);
        root.addOrReplaceChild("hair1", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-4f, -8f, -4f, 8f, 2f, 8f, d6)
                .addBox(-4f, -8f, 3f, 8f, 4f, 1f, d6), PartPose.ZERO);
        root.addOrReplaceChild("hair2", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-4f, -8f, -4f, 8f, 2f, 8f, d6)
                .addBox(-4f, -8f, 3f, 8f, 4f, 1f, d6)
                .addBox(-4f, -10f, -4f, 8f, 2f, 6f, d6), PartPose.ZERO);
        root.addOrReplaceChild("hair3", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-4f, -8f, -4f, 8f, 2f, 8f, d6)
                .addBox(-4f, -8f, 3f, 8f, 11f, 1f, d6)
                .addBox(-5f, -8f, -4f, 1f, 7f, 8f, d2)
                .addBox(4f, -8f, -4f, 1f, 7f, 8f, d2), PartPose.ZERO);
        root.addOrReplaceChild("hair4", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-4f, -9f, -4f, 4f, 2f, 4f, d6)
                .addBox(0f, -9f, -4f, 4f, 2f, 4f, d6)
                .addBox(-4f, -9f, 0f, 4f, 2f, 4f, d6)
                .addBox(0f, -9f, 0f, 4f, 2f, 4f, d6)
                .addBox(-4f, -8f, 3f, 8f, 3f, 1f, d6), PartPose.ZERO);
        root.addOrReplaceChild("hair5", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-4f, -8f, -4f, 8f, 1f, 8f, d6)
                .addBox(-4f, -8f, 3f, 8f, 5f, 1f, d6)
                .addBox(-4f, -9f, -4f, 8f, 1f, 6f, d3), PartPose.ZERO);

        root.addOrReplaceChild("nose", CubeListBuilder.create().texOffs(1, 13)
                .addBox(-1f, -1f, -0.5f, 2f, 2f, 1f), PartPose.offset(0f, -3f, -4.5f));
        root.addOrReplaceChild("chin", CubeListBuilder.create().texOffs(0, 13)
                .addBox(-2f, -0.5f, -0.5f, 4f, 1f, 1f), PartPose.offset(0f, -0.5f, -4.2f));
        root.addOrReplaceChild("jawL", CubeListBuilder.create().texOffs(0, 10)
                .addBox(-0.5f, -1f, -2f, 1f, 2f, 4f), PartPose.offset(-4.2f, -1.5f, 0f));
        root.addOrReplaceChild("jawR", CubeListBuilder.create().texOffs(0, 10)
                .addBox(-0.5f, -1f, -2f, 1f, 2f, 4f), PartPose.offset(4.2f, -1.5f, 0f));
        root.addOrReplaceChild("cheekL", CubeListBuilder.create().texOffs(0, 10)
                .addBox(-0.5f, -1f, -1.5f, 1f, 2f, 3f), PartPose.offset(-4.2f, -3.5f, -0.5f));
        root.addOrReplaceChild("cheekR", CubeListBuilder.create().texOffs(0, 10)
                .addBox(-0.5f, -1f, -1.5f, 1f, 2f, 3f), PartPose.offset(4.2f, -3.5f, -0.5f));
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void render(PoseStack ps, MultiBufferSource buf, int light, AbstractClientPlayer p,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        ClientHandler.Look l = ClientHandler.look.get(p.getUUID());
        if (l == null || p.isInvisible()) return;

        ps.pushPose();
        getParentModel().head.translateAndRotate(ps);
        int ov = LivingEntityRenderer.getOverlayCoords(p, 0.0f);

        VertexConsumer skin = buf.getBuffer(RenderType.entityCutoutNoCull(p.getSkinTextureLocation()));
        float ns = 0.6f + l.nose * 0.9f;
        nose.xScale = ns;
        nose.yScale = ns;
        nose.zScale = ns;
        nose.render(ps, skin, light, ov);

        chin.z = -4.2f - (l.chin - 0.5f) * 1.6f;
        chin.xScale = 0.8f + l.chin * 0.6f;
        chin.render(ps, skin, light, ov);

        float j = (l.jaw - 0.5f) * 1.2f;
        jawL.x = -4.2f - j;
        jawR.x = 4.2f + j;
        jawL.render(ps, skin, light, ov);
        jawR.render(ps, skin, light, ov);

        float c = (l.cheek - 0.5f) * 1.2f;
        cheekL.x = -4.2f - c;
        cheekR.x = 4.2f + c;
        cheekL.render(ps, skin, light, ov);
        cheekR.render(ps, skin, light, ov);

        if (p.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
            int style = Math.max(0, Math.min(hair.length - 1, l.hairStyle));
            float r = ((l.hairColor >> 16) & 0xFF) / 255f;
            float g = ((l.hairColor >> 8) & 0xFF) / 255f;
            float b = (l.hairColor & 0xFF) / 255f;
            VertexConsumer wc = buf.getBuffer(RenderType.entityCutoutNoCull(WHITE));
            hair[style].render(ps, wc, light, ov, r, g, b, 1.0f);
        }
        ps.popPose();
    }
}

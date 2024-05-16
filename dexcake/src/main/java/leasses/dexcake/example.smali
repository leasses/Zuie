# classes6.dex

.class public Lleasses/luwu/OriginalAccess;
.super Lleasses/luwu/Luwu;
.source "OriginalAccess.java"


# annotations
.annotation system Ldalvik/annotation/MemberClasses;
    value = {
        Lleasses/luwu/OriginalAccess$Original;
    }
.end annotation


# static fields
.field private static final a:[Ljava/lang/String;

.field private static final c:[[[Ljava/lang/Class;
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "[[[",
            "Ljava/lang/Class<",
            "*>;"
        }
    .end annotation
.end field


# direct methods
.method static constructor <clinit>()V
    .registers 6

    .line 11
    const/4 v0, 0x0

    new-array v1, v0, [Ljava/lang/String;

    sput-object v1, Lleasses/luwu/OriginalAccess;->a:[Ljava/lang/String;

    .line 12
    const/4 v1, 0x1

    new-array v2, v1, [[[Ljava/lang/Class;

    const/4 v3, 0x2

    new-array v3, v3, [[Ljava/lang/Class;

    new-array v4, v1, [Ljava/lang/Class;

    const-class v5, Ljava/lang/Integer;

    aput-object v5, v4, v0

    aput-object v4, v3, v0

    new-array v4, v1, [Ljava/lang/Class;

    const-class v5, Ljava/lang/Long;

    aput-object v5, v4, v0

    aput-object v4, v3, v1

    aput-object v3, v2, v0

    sput-object v2, Lleasses/luwu/OriginalAccess;->c:[[[Ljava/lang/Class;

    return-void
.end method

.method public constructor <init>()V
    .registers 5

    .line 17
    sget-object v0, Lleasses/luwu/OriginalAccess;->a:[Ljava/lang/String;

    const/4 v1, 0x0

    new-array v2, v1, [Ljava/lang/String;

    new-array v1, v1, [Ljava/lang/String;

    sget-object v3, Lleasses/luwu/OriginalAccess;->c:[[[Ljava/lang/Class;

    invoke-direct {p0, v0, v2, v1, v3}, Lleasses/luwu/Luwu;-><init>([Ljava/lang/String;[Ljava/lang/String;[Ljava/lang/String;[[[Ljava/lang/Class;)V

    .line 18
    return-void
.end method


# virtual methods
.method public access(Ljava/lang/Object;S[Ljava/lang/Object;)Ljava/lang/Object;
    .registers 8
    .param p1, "obj"  # Ljava/lang/Object;
    .param p2, "id"  # S
    .param p3, "args"  # [Ljava/lang/Object;

    .line 22
    const/4 v0, 0x0

    const/4 v1, 0x0

    packed-switch p2, :pswitch_data_3e

    .line 31
    new-instance v0, Ljava/lang/RuntimeException;

    invoke-direct {v0}, Ljava/lang/RuntimeException;-><init>()V

    throw v0

    .line 30
    :pswitch_b  #0x5
    new-instance v0, Lleasses/luwu/OriginalAccess$Original;

    invoke-direct {v0, v1}, Lleasses/luwu/OriginalAccess$Original;-><init>(Lleasses/luwu/OriginalAccess$Original-IA;)V

    move-object v1, v0

    goto :goto_3c

    .line 29
    :pswitch_12  #0x4
    new-instance v2, Lleasses/luwu/OriginalAccess$Original;

    aget-object v0, p3, v0

    check-cast v0, Ljava/lang/String;

    invoke-direct {v2, v0, v1}, Lleasses/luwu/OriginalAccess$Original;-><init>(Ljava/lang/String;Lleasses/luwu/OriginalAccess$Original-IA;)V

    move-object v1, v2

    goto :goto_3c

    .line 26
    :pswitch_1d  #0x3
    invoke-static {}, Lleasses/luwu/OriginalAccess$Original;->A()V

    .line 27
    goto :goto_3c

    .line 24
    :pswitch_21  #0x2
    move-object v1, p1

    check-cast v1, Lleasses/luwu/OriginalAccess$Original;

    aget-object v0, p3, v0

    check-cast v0, Ljava/lang/Double;

    invoke-virtual {v0}, Ljava/lang/Double;->doubleValue()D

    move-result-wide v2

    const/4 v0, 0x1

    aget-object v0, p3, v0

    check-cast v0, Ljava/lang/Long;

    invoke-virtual {v1, v2, v3, v0}, Lleasses/luwu/OriginalAccess$Original;->add(DLjava/lang/Long;)F

    move-result v0

    invoke-static {v0}, Ljava/lang/Float;->valueOf(F)Ljava/lang/Float;

    move-result-object v1

    goto :goto_3c

    .line 23
    :pswitch_3a  #0x1
    const-class v1, Lleasses/luwu/OriginalAccess$Original$A;

    .line 22
    :goto_3c
    return-object v1

    nop

    :pswitch_data_3e
    .packed-switch 0x1
        :pswitch_3a  #00000001
        :pswitch_21  #00000002
        :pswitch_1d  #00000003
        :pswitch_12  #00000004
        :pswitch_b  #00000005
    .end packed-switch
.end method
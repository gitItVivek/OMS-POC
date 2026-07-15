#!/usr/bin/env python3
"""Generate OMS-POC benchmark presentation — real names only, no A/B/C labels."""

from pptx import Presentation
from pptx.util import Inches, Pt
from pptx.dml.color import RGBColor
from pptx.enum.text import PP_ALIGN, MSO_ANCHOR
from pptx.enum.shapes import MSO_SHAPE

# ── palette ──────────────────────────────────────────────────────────────────
BG_DARK   = RGBColor(0x0F, 0x14, 0x19)
WHITE     = RGBColor(0xFF, 0xFF, 0xFF)
MUTED     = RGBColor(0x94, 0xA3, 0xB8)
ACCENT    = RGBColor(0x3B, 0x82, 0xF6)
SAGA_CLR  = RGBColor(0x8B, 0x5C, 0xF6)   # purple — saga + Apache Camel
CAMEL_CLR = RGBColor(0xF5, 0x9E, 0x0B)   # amber — camel-heavy
SI_CLR    = RGBColor(0x06, 0xB6, 0xD4)   # cyan — spring integration
GREEN     = RGBColor(0x10, 0xB9, 0x81)
RED       = RGBColor(0xF8, 0x71, 0x71)
CARD_BG   = RGBColor(0x1A, 0x23, 0x32)
LIGHT_BG  = RGBColor(0xF8, 0xFA, 0xFC)
DARK_TEXT = RGBColor(0x1E, 0x29, 0x3B)

SLIDE_W = Inches(13.333)
SLIDE_H = Inches(7.5)


def new_prs():
    prs = Presentation()
    prs.slide_width = SLIDE_W
    prs.slide_height = SLIDE_H
    return prs


def blank_slide(prs, dark=True):
    layout = prs.slide_layouts[6]  # blank
    slide = prs.slides.add_slide(layout)
    bg = slide.background.fill
    bg.solid()
    bg.fore_color.rgb = BG_DARK if dark else LIGHT_BG
    return slide


def add_textbox(slide, left, top, width, height, text, size=18, bold=False,
                color=WHITE, align=PP_ALIGN.LEFT, font_name="Segoe UI"):
    box = slide.shapes.add_textbox(left, top, width, height)
    tf = box.text_frame
    tf.word_wrap = True
    p = tf.paragraphs[0]
    p.text = text
    p.font.size = Pt(size)
    p.font.bold = bold
    p.font.color.rgb = color
    p.font.name = font_name
    p.alignment = align
    return box


def add_bullets(slide, left, top, width, height, items, size=14, color=MUTED,
                spacing=6, bold_first=False):
    box = slide.shapes.add_textbox(left, top, width, height)
    tf = box.text_frame
    tf.word_wrap = True
    for i, item in enumerate(items):
        p = tf.paragraphs[0] if i == 0 else tf.add_paragraph()
        p.text = item
        p.font.size = Pt(size)
        p.font.color.rgb = color
        p.font.name = "Segoe UI"
        p.font.bold = bold_first and i == 0
        p.space_after = Pt(spacing)
        p.level = 0
    return box


def slide_label(slide, text):
    add_textbox(slide, Inches(0.5), Inches(0.25), Inches(12), Inches(0.35),
                text, size=10, color=MUTED)


def slide_title(slide, text, top=Inches(0.55)):
    add_textbox(slide, Inches(0.5), top, Inches(12.3), Inches(0.7),
                text, size=28, bold=True, color=WHITE)


def add_rect(slide, left, top, width, height, fill, line=None, text="",
             text_size=11, text_color=WHITE, bold=False):
    shape = slide.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, left, top, width, height)
    shape.fill.solid()
    shape.fill.fore_color.rgb = fill
    if line:
        shape.line.color.rgb = line
        shape.line.width = Pt(1.5)
    else:
        shape.line.fill.background()
    if text:
        tf = shape.text_frame
        tf.word_wrap = True
        tf.vertical_anchor = MSO_ANCHOR.MIDDLE
        p = tf.paragraphs[0]
        p.text = text
        p.font.size = Pt(text_size)
        p.font.color.rgb = text_color
        p.font.bold = bold
        p.font.name = "Segoe UI"
        p.alignment = PP_ALIGN.CENTER
    return shape


def add_arrow(slide, x1, y1, x2, y2, color=MUTED):
    conn = slide.shapes.add_connector(1, x1, y1, x2, y2)  # straight connector
    conn.line.color.rgb = color
    conn.line.width = Pt(1.5)


# ── SLIDE 1: Title ───────────────────────────────────────────────────────────
def slide_01_title(prs):
    s = blank_slide(prs)
    add_textbox(s, Inches(0.8), Inches(1.6), Inches(11.5), Inches(1.2),
                "OMS-POC: Orchestration Benchmark", size=36, bold=True, color=WHITE,
                align=PP_ALIGN.CENTER)
    add_textbox(s, Inches(0.8), Inches(2.7), Inches(11.5), Inches(0.6),
                "Apache Camel vs Spring Integration — and where Saga fits in",
                size=20, color=MUTED, align=PP_ALIGN.CENTER)
    add_textbox(s, Inches(0.8), Inches(3.5), Inches(11.5), Inches(0.5),
                "Three live implementations · Same Kafka business flow · JMeter + Postman results",
                size=14, color=MUTED, align=PP_ALIGN.CENTER)

    badges = [
        ("Saga-heavy + Apache Camel", SAGA_CLR),
        ("Camel-heavy pipeline", CAMEL_CLR),
        ("Saga + Spring Integration", SI_CLR),
    ]
    bx = Inches(1.8)
    for label, clr in badges:
        add_rect(s, bx, Inches(4.4), Inches(3.2), Inches(0.45), CARD_BG, clr, label, 11, clr, True)
        bx += Inches(3.4)

    add_textbox(s, Inches(0.8), Inches(5.3), Inches(11.5), Inches(0.8),
                "Two decisions this deck answers:\n"
                "① Orchestration: Java Saga (not Camel-heavy)  ·  "
                "② EIP adapter: Apache Camel or Spring Integration",
                size=14, color=GREEN, align=PP_ALIGN.CENTER)


# ── SLIDE 2: Goal vs what we built ──────────────────────────────────────────
def slide_02_goal(prs):
    s = blank_slide(prs)
    slide_label(s, "OMS-POC Benchmark")
    slide_title(s, "Original goal → what we actually built")

    add_rect(s, Inches(0.5), Inches(1.5), Inches(5.9), Inches(2.6), CARD_BG, ACCENT,
             "Original question", 14, ACCENT, True)
    add_bullets(s, Inches(0.7), Inches(1.95), Inches(5.5), Inches(2.0), [
        "Compare Apache Camel vs Spring Integration",
        "as EIP frameworks for multi-microservice OMS",
        "Same place-order flow, same Kafka backbone",
    ], size=13, color=WHITE)

    add_rect(s, Inches(6.9), Inches(1.5), Inches(5.9), Inches(2.6), CARD_BG, CAMEL_CLR,
             "What happened during implementation", 14, CAMEL_CLR, True)
    add_bullets(s, Inches(7.1), Inches(1.95), Inches(5.5), Inches(2.0), [
        "Added Saga pattern for coordinating services",
        "Ended up with 3 implementations — not 2",
        "Demo confusion: similar endpoints, different brains",
        "This deck fixes that — one slide per implementation",
    ], size=13, color=WHITE)

    add_rect(s, Inches(0.5), Inches(4.4), Inches(12.3), Inches(2.7), CARD_BG, SAGA_CLR)
    add_textbox(s, Inches(0.8), Inches(4.52), Inches(11.7), Inches(0.4),
                "What 'Apache Camel' means in this POC", size=15, bold=True, color=SAGA_CLR)
    add_textbox(s, Inches(0.8), Inches(4.95), Inches(11.7), Inches(2.0),
                "When we say Apache Camel, we do NOT mean Camel owns the business flow.\n\n"
                "Apache Camel = protocol bridging only — Kafka in → JSON transform → hand off to Java.\n"
                "Business logic (orchestration, compensation, next steps) = Java, following the Saga pattern.\n"
                "SagaOrchestratorServiceImpl is the brain. OrderSagaRoutes.java is the Kafka bridge.\n\n"
                "Camel-heavy pipeline is a separate experiment where Camel owns orchestration — not what we recommend.",
                size=12, color=WHITE)


# ── SLIDE 3: Baseline EIP question (early) ───────────────────────────────────
def slide_03_baseline_eip(prs):
    s = blank_slide(prs)
    slide_label(s, "The baseline question — why this POC exists")
    slide_title(s, "Camel vs Spring Integration: what are we actually comparing?")

    add_textbox(s, Inches(0.5), Inches(1.25), Inches(12.3), Inches(0.55),
                "The POC was built to answer: which EIP framework should sit in integration-service "
                "alongside Kafka in a multi-microservice OMS?",
                size=14, color=WHITE, align=PP_ALIGN.CENTER)

    add_rect(s, Inches(0.5), Inches(1.95), Inches(5.9), Inches(2.5), CARD_BG, SAGA_CLR,
             "Apache Camel", 14, SAGA_CLR, True)
    add_bullets(s, Inches(0.7), Inches(2.4), Inches(5.5), Inches(1.9), [
        "Standalone integration framework with 300+ connectors",
        "Route DSL (Java/XML) — Kafka, HTTP, file, JMS, etc.",
        "In our POC: Kafka protocol bridging → JSON → Java saga bean",
        "File: OrderSagaRoutes.java (~47 lines, 5 routes)",
    ], size=11, color=WHITE)

    add_rect(s, Inches(6.9), Inches(1.95), Inches(5.9), Inches(2.5), CARD_BG, SI_CLR,
             "Spring Integration", 14, SI_CLR, True)
    add_bullets(s, Inches(7.1), Inches(2.4), Inches(5.5), Inches(1.9), [
        "Native Spring module — ships with Spring Boot",
        "Channel + flow model — Kafka inbound → transform → handler",
        "In our POC: same role as Apache Camel — adapter only",
        "File: OrderSagaIntegrationFlows.java",
    ], size=11, color=WHITE)

    add_rect(s, Inches(0.5), Inches(4.7), Inches(12.3), Inches(2.3), CARD_BG, GREEN)
    add_textbox(s, Inches(0.8), Inches(4.85), Inches(11.7), Inches(0.35),
                "How we made the comparison fair", size=14, bold=True, color=GREEN)
    add_bullets(s, Inches(0.8), Inches(5.25), Inches(11.5), Inches(1.6), [
        "SAME Java saga brain (SagaOrchestratorServiceImpl) — identical business logic for both",
        "SAME Kafka topics (oms.*), SAME DB table (saga_instances), SAME command publisher",
        "ONLY difference: who wires Kafka events back into Java — Camel routes or SI flows",
        "Camel-heavy pipeline is a separate experiment — NOT part of this EIP comparison",
    ], size=12, color=WHITE)


# ── SLIDE 4: System architecture ────────────────────────────────────────────
def slide_04_architecture(prs):
    s = blank_slide(prs)
    slide_label(s, "OMS-POC Benchmark")
    slide_title(s, "System architecture — 6 microservices + Kafka")

    # Top row — client
    add_rect(s, Inches(5.5), Inches(1.35), Inches(2.3), Inches(0.55), ACCENT, None,
             "Client / JMeter / Postman", 10, WHITE, True)

    # Integration service (center, large)
    add_rect(s, Inches(3.8), Inches(2.2), Inches(5.7), Inches(1.5), CARD_BG, SAGA_CLR,
             "integration-service (8085)\n3 orchestration implementations live here",
             12, WHITE, True)

    # Kafka bus
    add_rect(s, Inches(1.0), Inches(4.2), Inches(11.3), Inches(0.65), RGBColor(0x22, 0x55, 0x44), GREEN,
             "Apache Kafka 3.9  —  oms.* topics (saga paths)  |  oms.camel.* topics (camel-heavy path)",
             11, WHITE, True)

    # Domain services
    services = [
        ("identity-service\n8086 · JWT", Inches(0.5)),
        ("order-service\n8081", Inches(3.3)),
        ("inventory-service\n8084", Inches(6.1)),
        ("fulfillment-service\n8082", Inches(8.9)),
        ("notification-service", Inches(11.0)),
    ]
    for label, x in services:
        add_rect(s, x, Inches(5.3), Inches(2.0), Inches(0.85), CARD_BG, ACCENT, label, 9, MUTED)

    # PostgreSQL
    add_rect(s, Inches(4.5), Inches(6.5), Inches(4.3), Inches(0.55), CARD_BG, MUTED,
             "PostgreSQL — saga_instances  |  pipeline_runs", 10, MUTED)

    add_bullets(s, Inches(0.5), Inches(1.35), Inches(3.0), Inches(0.8), [
        "Domain services use plain @KafkaListener",
        "No Camel or SI in order/inventory/fulfillment",
    ], size=10, color=MUTED)


# ── SLIDE 5: Business flow ──────────────────────────────────────────────────
def slide_05_flow(prs):
    s = blank_slide(prs)
    slide_label(s, "OMS-POC Benchmark")
    slide_title(s, "Same business flow — all three implementations")

    steps = [
        ("1", "Place\norder", SAGA_CLR),
        ("2", "Create\norder", ACCENT),
        ("3", "Reserve\nstock", ACCENT),
        ("4", "Confirm\norder", ACCENT),
        ("5", "Start\nfulfillment", ACCENT),
        ("6", "Shipment\nevent", GREEN),
    ]
    x = Inches(0.4)
    for num, label, clr in steps:
        add_rect(s, x, Inches(2.0), Inches(1.85), Inches(1.1), CARD_BG, clr,
                 f"{num}\n{label}", 12, WHITE, True)
        if num != "6":
            add_textbox(s, x + Inches(1.85), Inches(2.35), Inches(0.35), Inches(0.4),
                        "→", size=18, color=MUTED, align=PP_ALIGN.CENTER)
        x += Inches(2.15)

    add_rect(s, Inches(0.5), Inches(3.6), Inches(12.3), Inches(3.2), CARD_BG, MUTED)
    add_textbox(s, Inches(0.8), Inches(3.75), Inches(11.7), Inches(0.4),
                "Bench API endpoints (integration-service only)", size=14, bold=True, color=WHITE)
    rows = [
        ("Saga-heavy + Apache Camel", "POST /api/bench/place-order/saga", SAGA_CLR),
        ("Camel-heavy pipeline", "POST /api/bench/place-order/camel", CAMEL_CLR),
        ("Saga + Spring Integration", "POST /api/bench/place-order/spring-integration", SI_CLR),
    ]
    y = Inches(4.25)
    for name, endpoint, clr in rows:
        add_rect(s, Inches(0.8), y, Inches(3.5), Inches(0.5), clr, None, name, 11, WHITE, True)
        add_textbox(s, Inches(4.5), y + Inches(0.05), Inches(8.0), Inches(0.45),
                    endpoint, size=12, color=MUTED)
        y += Inches(0.65)

    add_textbox(s, Inches(0.8), Inches(6.3), Inches(11.5), Inches(0.5),
                "Status check (all three): GET /api/bench/place-order/{orderId}/status",
                size=12, color=GREEN)


# ── SLIDE 6: Saga-heavy + Apache Camel ────────────────────────────────────────
def slide_06_saga_camel(prs):
    s = blank_slide(prs)
    slide_label(s, "Implementation 1 of 3")
    slide_title(s, "Saga-heavy + Apache Camel", top=Inches(0.5))
    add_textbox(s, Inches(0.5), Inches(1.15), Inches(12), Inches(0.4),
                "~80% Java saga orchestration  ·  ~20% Apache Camel for Kafka protocol bridging",
                size=14, color=SAGA_CLR, bold=True)

    add_rect(s, Inches(0.5), Inches(1.7), Inches(5.8), Inches(4.8), CARD_BG, SAGA_CLR)
    add_textbox(s, Inches(0.7), Inches(1.85), Inches(5.4), Inches(0.35),
                "Flow", size=13, bold=True, color=SAGA_CLR)
    flow = (
        "HTTP → SagaOrchestratorServiceImpl (Java)\n"
        "     → saves saga_instances in PostgreSQL\n"
        "     → SagaCommandPublisher (KafkaTemplate)\n"
        "     → oms.order.* / oms.inventory.* / oms.fulfillment.*\n\n"
        "Domain services react → publish events\n\n"
        "OrderSagaRoutes (Apache Camel — protocol bridging):\n"
        "  Kafka in → JSON unmarshal → bean method\n"
        "  → back to SagaOrchestratorServiceImpl\n\n"
        "Compensation, validation, next-step logic = Java"
    )
    add_textbox(s, Inches(0.7), Inches(2.2), Inches(5.4), Inches(4.0), flow, size=11, color=MUTED)

    add_rect(s, Inches(6.6), Inches(1.7), Inches(6.2), Inches(4.8), CARD_BG, SAGA_CLR)
    add_textbox(s, Inches(6.8), Inches(1.85), Inches(5.8), Inches(0.35),
                "Key files & Camel EIP used", size=13, bold=True, color=SAGA_CLR)
    add_bullets(s, Inches(6.8), Inches(2.25), Inches(5.8), Inches(4.0), [
        "SagaOrchestratorServiceImpl.java — the brain",
        "SagaCommandPublisher.java — publishes commands",
        "OrderSagaRoutes.java — 5 Kafka listeners (protocol bridging only)",
        "saga_instances table — saga state",
        "",
        "Camel EIP: Kafka consumer, JSON, bean delegation",
        "NOT used: split, choice, Kafka producer in routes",
        "",
        "Apache Camel bridges Kafka to Java — SagaOrchestratorServiceImpl is the manager",
    ], size=11, color=WHITE)


# ── SLIDE 7: Camel-heavy pipeline ───────────────────────────────────────────
def slide_07_camel_heavy(prs):
    s = blank_slide(prs)
    slide_label(s, "Implementation 2 of 3")
    slide_title(s, "Camel-heavy pipeline", top=Inches(0.5))
    add_textbox(s, Inches(0.5), Inches(1.15), Inches(12), Inches(0.4),
                "~80% Camel DSL orchestration  ·  ~20% Java processor beans",
                size=14, color=CAMEL_CLR, bold=True)

    add_rect(s, Inches(0.5), Inches(1.7), Inches(5.8), Inches(4.8), CARD_BG, CAMEL_CLR)
    add_textbox(s, Inches(0.7), Inches(1.85), Inches(5.4), Inches(0.35),
                "Flow", size=13, bold=True, color=CAMEL_CLR)
    flow = (
        "HTTP → direct:bench-place-order (in-JVM only)\n"
        "     → PlaceOrderCamelPipelineRoutes (Camel DSL)\n"
        "     → Camel publishes to oms.camel.* topics\n\n"
        "Each Kafka event → separate Camel route\n"
        "  split — validate line items in parallel\n"
        "  choice — compensation on stock failure\n"
        "  to(kafka:...) — publish next command\n\n"
        "pipeline_runs table — pipeline state\n\n"
        "Orchestration, branching, publishing = Camel"
    )
    add_textbox(s, Inches(0.7), Inches(2.2), Inches(5.4), Inches(4.0), flow, size=11, color=MUTED)

    add_rect(s, Inches(6.6), Inches(1.7), Inches(6.2), Inches(4.8), CARD_BG, CAMEL_CLR)
    add_textbox(s, Inches(6.8), Inches(1.85), Inches(5.8), Inches(0.35),
                "Key files & extra Camel EIP", size=13, bold=True, color=CAMEL_CLR)
    add_bullets(s, Inches(6.8), Inches(2.25), Inches(5.8), Inches(4.0), [
        "PlaceOrderCamelPipelineRoutes.java — 6 routes, the brain",
        "PlaceOrderPipelineProcessor.java — helper beans",
        "OmsCamelPipelineKafkaTopics.java — oms.camel.* namespace",
        "pipeline_runs table — separate from saga_instances",
        "",
        "Extra EIP: direct, split, parallelProcessing, choice",
        "Kafka producers inside Camel routes",
        "",
        "Isolated topic namespace — runs side-by-side with saga paths",
    ], size=11, color=WHITE)


# ── SLIDE 8: Saga + Spring Integration ───────────────────────────────────────
def slide_08_saga_si(prs):
    s = blank_slide(prs)
    slide_label(s, "Implementation 3 of 3")
    slide_title(s, "Saga + Spring Integration", top=Inches(0.5))
    add_textbox(s, Inches(0.5), Inches(1.15), Inches(12), Inches(0.4),
                "Same Java saga brain as Saga-heavy + Apache Camel — only the event adapter changes",
                size=14, color=SI_CLR, bold=True)

    add_rect(s, Inches(0.5), Inches(1.7), Inches(5.8), Inches(4.8), CARD_BG, SI_CLR)
    add_textbox(s, Inches(0.7), Inches(1.85), Inches(5.4), Inches(0.35),
                "Flow", size=13, bold=True, color=SI_CLR)
    flow = (
        "HTTP → SpringIntegrationBenchController\n"
        "     → same SagaOrchestratorServiceImpl\n"
        "     → same saga_instances table\n"
        "     → same SagaCommandPublisher\n"
        "     → same oms.* Kafka topics\n\n"
        "OrderSagaIntegrationFlows (Spring Integration):\n"
        "  Kafka inbound → JSON transform → saga bean\n\n"
        "event_adapter = SPRING_INTEGRATION on saga row\n"
        "Orchestrator ignores events from wrong adapter"
    )
    add_textbox(s, Inches(0.7), Inches(2.2), Inches(5.4), Inches(4.0), flow, size=11, color=MUTED)

    add_rect(s, Inches(6.6), Inches(1.7), Inches(6.2), Inches(2.2), CARD_BG, SI_CLR)
    add_textbox(s, Inches(6.8), Inches(1.85), Inches(5.8), Inches(0.35),
                "What stayed the same vs what changed", size=13, bold=True, color=SI_CLR)
    add_bullets(s, Inches(6.8), Inches(2.25), Inches(5.8), Inches(1.5), [
        "SAME: SagaOrchestratorServiceImpl, compensation, oms.* topics",
        "CHANGED: Kafka→Java wiring uses SI flows instead of Camel routes",
        "SI lives ONLY in integration-service — domain services untouched",
    ], size=11, color=WHITE)

    add_rect(s, Inches(6.6), Inches(4.1), Inches(6.2), Inches(2.4), CARD_BG, GREEN)
    add_textbox(s, Inches(6.8), Inches(4.25), Inches(5.8), Inches(0.35),
                "When to pick this over Apache Camel", size=13, bold=True, color=GREEN)
    add_bullets(s, Inches(6.8), Inches(4.65), Inches(5.8), Inches(1.6), [
        "Team is Spring-native, no Camel expertise",
        "Want zero extra Camel dependency in integration-service",
        "Benchmark showed comparable reliability — adapter is a team choice",
    ], size=11, color=WHITE)


# ── SLIDE 9: Side-by-side comparison ─────────────────────────────────────────
def slide_09_comparison(prs):
    s = blank_slide(prs)
    slide_label(s, "The slide that matters")
    slide_title(s, "Who orchestrates? — side-by-side")

    headers = ["Concern", "Saga-heavy +\nApache Camel", "Camel-heavy\npipeline", "Saga +\nSpring Integration"]
    col_w = [Inches(2.8), Inches(2.9), Inches(2.9), Inches(2.9)]
    x_positions = [Inches(0.5)]
    for w in col_w[:-1]:
        x_positions.append(x_positions[-1] + w)

    y = Inches(1.45)
    colors = [MUTED, SAGA_CLR, CAMEL_CLR, SI_CLR]
    for i, h in enumerate(headers):
        add_rect(s, x_positions[i], y, col_w[i], Inches(0.7), CARD_BG, colors[i],
                 h, 10, colors[i], True)

    rows = [
        ("Who is the brain?", "Java", "Camel DSL", "Java"),
        ("Publish commands", "KafkaTemplate", "Camel to(kafka)", "KafkaTemplate"),
        ("Consume events", "Camel routes", "Camel routes", "SI flows"),
        ("Line item validation", "Java loop", "Camel split", "Java loop"),
        ("Compensation", "Java method", "Camel choice", "Java method"),
        ("State table", "saga_instances", "pipeline_runs", "saga_instances"),
        ("Kafka topics", "oms.*", "oms.camel.*", "oms.*"),
        ("Unit testable?", "Yes — Java", "Harder — routes", "Yes — Java"),
    ]
    y = Inches(2.25)
    for row in rows:
        for i, cell in enumerate(row):
            clr = WHITE if i == 0 else (GREEN if cell == "Java" else MUTED)
            if i > 0 and cell in ("Java", "KafkaTemplate", "saga_instances", "oms.*", "Yes — Java"):
                clr = GREEN
            if i == 2 and row[0] != "Who is the brain?":
                clr = CAMEL_CLR if "Camel" in cell else MUTED
            add_rect(s, x_positions[i], y, col_w[i], Inches(0.52), CARD_BG if i == 0 else RGBColor(0x12, 0x1A, 0x28),
                     None, cell, 9, clr, i == 0)
        y += Inches(0.56)


# ── SLIDE 10: Saga vs Camel pipeline (concept) ───────────────────────────────
def slide_10_saga_concept(prs):
    s = blank_slide(prs)
    slide_label(s, "Concept deep-dive")
    slide_title(s, "Saga orchestration vs Camel pipeline orchestration")

    add_rect(s, Inches(0.5), Inches(1.45), Inches(5.9), Inches(5.3), CARD_BG, SAGA_CLR,
             "Saga orchestration (Java-heavy)", 14, SAGA_CLR, True)
    add_bullets(s, Inches(0.7), Inches(1.95), Inches(5.5), Inches(4.5), [
        "Business steps live in a Java service class",
        "Each Kafka event triggers a named method (onStockReserved, etc.)",
        "State persisted in DB — saga_instances tracks current step",
        "Compensation = plain if/else in Java",
        "",
        "Best for: complex multi-step workflows across microservices",
        "OMS domain rules stay where developers expect them",
        "Easy to unit test without starting Camel or Kafka",
        "Stack traces point to Java — easier debugging",
    ], size=12, color=WHITE)

    add_rect(s, Inches(6.9), Inches(1.45), Inches(5.9), Inches(5.3), CARD_BG, CAMEL_CLR,
             "Camel pipeline orchestration (DSL-heavy)", 14, CAMEL_CLR, True)
    add_bullets(s, Inches(7.1), Inches(1.95), Inches(5.5), Inches(4.5), [
        "Business flow expressed as Camel routes (XML/Java DSL)",
        "split, choice, multicast are first-class in routes",
        "State in pipeline_runs — separate from saga model",
        "Compensation = choice/when blocks in DSL",
        "",
        "Best for: integration-centric flows, protocol bridging",
        "Good when team is Camel-native and flow is mostly wiring",
        "Harder to unit test — logic spread across routes + beans",
        "Our POC: no performance win, higher HTTP latency",
    ], size=12, color=WHITE)

    add_textbox(s, Inches(0.5), Inches(6.85), Inches(12.3), Inches(0.45),
                "For core OMS saga logic in a complex microservice project → Saga orchestration wins on clarity and testability",
                size=13, bold=True, color=GREEN, align=PP_ALIGN.CENTER)


# ── SLIDE 11: Apache Camel vs Spring Integration (concept) ─────────────────────
def slide_11_adapter_concept(prs):
    s = blank_slide(prs)
    slide_label(s, "Concept deep-dive")
    slide_title(s, "Apache Camel vs Spring Integration — same adapter role")

    add_textbox(s, Inches(0.5), Inches(1.2), Inches(12.3), Inches(0.5),
                "Both sit in the SAME role: Kafka protocol bridging → hand off to the Java saga brain. "
                "Neither owns orchestration or business logic.",
                size=13, color=MUTED, align=PP_ALIGN.CENTER)

    add_rect(s, Inches(0.5), Inches(1.85), Inches(5.9), Inches(4.5), CARD_BG, SAGA_CLR,
             "Apache Camel (OrderSagaRoutes)", 13, SAGA_CLR, True)
    add_bullets(s, Inches(0.7), Inches(2.3), Inches(5.5), Inches(3.8), [
        "~47 lines — 5 identical mini-routes",
        "Kafka consumer → JSON unmarshal → bean call",
        "Camel EIP ecosystem available if needed later",
        "Team already using Camel elsewhere in the stack",
        "JMeter POST mean: 18 ms",
    ], size=12, color=WHITE)

    add_rect(s, Inches(6.9), Inches(1.85), Inches(5.9), Inches(4.5), CARD_BG, SI_CLR,
             "Spring Integration (OrderSagaIntegrationFlows)", 13, SI_CLR, True)
    add_bullets(s, Inches(7.1), Inches(2.3), Inches(5.5), Inches(3.8), [
        "Native Spring — fits Boot stack without Camel",
        "Kafka inbound channel → transform → saga bean",
        "No extra Camel dependency in integration-service",
        "Same saga logic, same topics, same DB table",
        "JMeter POST mean: 28 ms — same order of magnitude",
    ], size=12, color=WHITE)

    add_rect(s, Inches(0.5), Inches(6.55), Inches(12.3), Inches(0.65), CARD_BG, GREEN)
    add_textbox(s, Inches(0.8), Inches(6.65), Inches(11.7), Inches(0.45),
                "This is the baseline EIP comparison. Next slides show WHEN to pick Apache Camel vs Spring Integration.",
                size=14, bold=True, color=GREEN, align=PP_ALIGN.CENTER)


# ── SLIDE 12: EIP decision guide (middle) ────────────────────────────────────
def slide_12_eip_decision_guide(prs):
    s = blank_slide(prs)
    slide_label(s, "Baseline decision — the question this POC was built to answer")
    slide_title(s, "Saga-heavy + Apache Camel  vs  Saga + Spring Integration — when to choose?")

    add_textbox(s, Inches(0.5), Inches(1.2), Inches(12.3), Inches(0.45),
                "Both use the same Java saga brain. You are choosing the Kafka event adapter — not re-writing orchestration.",
                size=13, color=MUTED, align=PP_ALIGN.CENTER)

    add_rect(s, Inches(0.5), Inches(1.75), Inches(5.9), Inches(4.9), CARD_BG, SAGA_CLR,
             "Choose Saga-heavy + Apache Camel when…", 13, SAGA_CLR, True)
    add_bullets(s, Inches(0.7), Inches(2.2), Inches(5.5), Inches(4.2), [
        "Your org already runs Camel for integrations (files, HTTP, legacy, ESB migration)",
        "You need Camel's 300+ connectors in the same integration-service later",
        "Team has Camel skills — onboarding cost is near zero",
        "You want a route DSL for the adapter layer (some teams prefer it over SI XML/Java flows)",
        "Enterprise integration standards mandate Camel as the integration layer",
        "Our POC: lowest HTTP latency (18 ms POST, p99 21 ms Postman) — nice bonus, not the main reason",
        "",
        "Concrete POC file: OrderSagaRoutes.java — 5 Kafka listeners (protocol bridging only)",
    ], size=11, color=WHITE)

    add_rect(s, Inches(6.9), Inches(1.75), Inches(5.9), Inches(4.9), CARD_BG, SI_CLR,
             "Choose Saga + Spring Integration when…", 13, SI_CLR, True)
    add_bullets(s, Inches(7.1), Inches(2.2), Inches(5.5), Inches(4.2), [
        "Team is Spring Boot–native — no Camel expertise and no plan to hire for it",
        "You want one framework on the classpath (Boot + Integration, skip Camel dependency)",
        "Simpler dependency tree — integration-service already has spring-kafka + SI starters",
        "Channel/flow model feels natural to developers who know @Service and @Bean",
        "You may add Camel later for specific connectors — saga brain stays unchanged",
        "Our POC: 0% errors, 28 ms POST mean, p99 30 ms — fully production-viable at this scale",
        "",
        "Concrete POC file: OrderSagaIntegrationFlows.java — same 5 events as Camel",
    ], size=11, color=WHITE)

    add_rect(s, Inches(0.5), Inches(6.75), Inches(12.3), Inches(0.55), CARD_BG, ACCENT)
    add_textbox(s, Inches(0.8), Inches(6.82), Inches(11.7), Inches(0.4),
                "Do NOT decide on ~10 ms latency gap. Decide on team skills, org standards, and future connector needs.",
                size=13, bold=True, color=ACCENT, align=PP_ALIGN.CENTER)


# ── SLIDE 13: Head-to-head saga paths only ───────────────────────────────────
def slide_13_eip_head_to_head(prs):
    s = blank_slide(prs)
    slide_label(s, "Baseline comparison — saga paths only (apples to apples)")
    slide_title(s, "Saga-heavy + Apache Camel  vs  Saga + Spring Integration — head-to-head")

    headers = ["Criterion", "Saga-heavy + Apache Camel", "Saga + Spring Integration"]
    col_w = [Inches(3.2), Inches(4.3), Inches(4.3)]
    x_pos = [Inches(0.5), Inches(3.7), Inches(8.0)]
    y = Inches(1.4)
    clrs = [MUTED, SAGA_CLR, SI_CLR]
    for i, h in enumerate(headers):
        add_rect(s, x_pos[i], y, col_w[i], Inches(0.65), CARD_BG, clrs[i], h, 10, clrs[i], True)

    rows = [
        ("Saga brain", "SagaOrchestratorServiceImpl", "SagaOrchestratorServiceImpl (identical)"),
        ("Business logic", "Same compensation, same steps", "Same compensation, same steps"),
        ("Kafka topics", "oms.*", "oms.* (identical)"),
        ("DB state", "saga_instances", "saga_instances (identical)"),
        ("Command publishing", "KafkaTemplate (Java)", "KafkaTemplate (Java)"),
        ("Event adapter", "OrderSagaRoutes (Camel)", "OrderSagaIntegrationFlows (SI)"),
        ("Extra dependency", "camel-kafka, camel-jackson", "spring-integration-kafka (already in Boot)"),
        ("JMeter POST mean", "18 ms", "28 ms"),
        ("JMeter GET status mean", "22 ms", "25 ms"),
        ("Postman p99", "21 ms", "30 ms"),
        ("Error rate (200 orders)", "0%", "0%"),
        ("Switch cost later", "Low — swap adapter, keep saga", "Low — swap adapter, keep saga"),
    ]
    y = Inches(2.15)
    for row in rows:
        for i, cell in enumerate(row):
            c = WHITE if i == 0 else MUTED
            if row[0] == "Saga brain" and i > 0:
                c = GREEN
            if row[0] in ("JMeter POST mean", "Postman p99") and i == 1:
                c = GREEN
            add_rect(s, x_pos[i], y, col_w[i], Inches(0.42),
                     CARD_BG if i == 0 else RGBColor(0x12, 0x1A, 0x28), None, cell, 9, c, i == 0)
        y += Inches(0.46)

    add_textbox(s, Inches(0.5), Inches(6.85), Inches(12.3), Inches(0.45),
                "Verdict on EIP choice: both are valid. Pick Camel if org/team is Camel-oriented; pick SI if Spring-native.",
                size=13, bold=True, color=GREEN, align=PP_ALIGN.CENTER)


# ── SLIDE 14: Benchmark results ─────────────────────────────────────────────
def slide_14_results(prs):
    s = blank_slide(prs)
    slide_label(s, "Benchmark results")
    slide_title(s, "JMeter + Postman — all three implementations")

    add_textbox(s, Inches(0.5), Inches(1.2), Inches(12.3), Inches(0.4),
                "Profile: 10 threads × 20 loops = 200 orders each · 0% errors across all runs",
                size=12, color=MUTED)

    # JMeter table header
    headers = ["Metric", "Saga-heavy +\nApache Camel", "Camel-heavy\npipeline", "Saga +\nSpring Integration"]
    col_w = [Inches(2.5), Inches(2.9), Inches(2.9), Inches(2.9)]
    x_pos = [Inches(0.5), Inches(3.0), Inches(5.9), Inches(8.8)]
    y = Inches(1.65)
    clrs = [MUTED, SAGA_CLR, CAMEL_CLR, SI_CLR]
    for i, h in enumerate(headers):
        add_rect(s, x_pos[i], y, col_w[i], Inches(0.65), CARD_BG, clrs[i], h, 9, clrs[i], True)

    jmeter_rows = [
        ("POST mean (JMeter)", "18 ms", "33 ms", "28 ms"),
        ("GET status mean", "22 ms", "40 ms", "25 ms"),
        ("POST max spike", "50 ms", "253 ms", "560 ms"),
        ("Error rate", "0%", "0%", "0%"),
        ("Manual elapsedMs", "~262 ms", "~212 ms", "—"),
    ]
    y = Inches(2.4)
    for row in jmeter_rows:
        for i, cell in enumerate(row):
            c = WHITE if i == 0 else (GREEN if i == 1 else (RED if i == 2 and "253" in cell else MUTED))
            add_rect(s, x_pos[i], y, col_w[i], Inches(0.48), RGBColor(0x12, 0x1A, 0x28), None, cell, 10, c, i == 0)
        y += Inches(0.52)

    add_rect(s, Inches(0.5), Inches(5.1), Inches(12.3), Inches(2.0), CARD_BG, ACCENT)
    add_textbox(s, Inches(0.8), Inches(5.2), Inches(11.7), Inches(0.35),
                "Postman concurrent load (20 users, 1 min, all three endpoints together)", size=13, bold=True, color=ACCENT)
    postman = (
        "16,430 total requests · 279 req/s · 9 ms avg · p99 = 26 ms · 0% errors\n\n"
        "Saga-heavy + Apache Camel:  avg 8 ms, p99 21 ms, max 64 ms\n"
        "Camel-heavy pipeline:     avg 8 ms, p99 24 ms, max 137 ms  (higher tail spikes)\n"
        "Saga + Spring Integration: avg 12 ms, p99 30 ms, max 74 ms"
    )
    add_textbox(s, Inches(0.8), Inches(5.6), Inches(11.7), Inches(1.4), postman, size=11, color=WHITE)


# ── SLIDE 15: Why Saga-heavy + Apache Camel ───────────────────────────────────
def slide_15_why_saga(prs):
    s = blank_slide(prs)
    slide_label(s, "Recommendation")
    slide_title(s, "Why Saga-heavy + Apache Camel for complex microservices")

    reasons = [
        ("Readable business logic",
         "Compensation, branching, and step transitions are plain Java — OMS domain developers can read and change them without learning Camel DSL."),
        ("Unit testable orchestration",
         "SagaOrchestratorServiceImpl tests run without spinning Camel routes or Kafka. Mock the publisher, test every saga step."),
        ("Easier debugging in production",
         "Stack traces point to Java methods, not route IDs. On-call engineers trace failures in familiar code."),
        ("Camel does protocol bridging only",
         "Apache Camel: Kafka in → JSON → bean. Business rules stay in Java saga. ~20% Camel, ~80% Java."),
        ("Benchmark-backed — not slower",
         "JMeter: 18 ms POST mean vs 33 ms Camel-heavy. Postman: lowest p99 (21 ms) and lowest max (64 ms)."),
        ("Scales with team growth",
         "New saga steps = new Java method + publisher call. No growing choice/split blocks in DSL."),
    ]
    y = Inches(1.4)
    for i, (title, desc) in enumerate(reasons):
        col = i % 2
        row = i // 2
        x = Inches(0.5) if col == 0 else Inches(6.7)
        yy = Inches(1.4) + Inches(1.85) * row
        add_rect(s, x, yy, Inches(5.9), Inches(1.65), CARD_BG, SAGA_CLR)
        add_textbox(s, x + Inches(0.15), yy + Inches(0.1), Inches(5.6), Inches(0.35),
                    title, size=12, bold=True, color=SAGA_CLR)
        add_textbox(s, x + Inches(0.15), yy + Inches(0.45), Inches(5.6), Inches(1.1),
                    desc, size=10, color=MUTED)


# ── SLIDE 16: Final verdict — two decisions ─────────────────────────────────
def slide_16_verdict(prs):
    s = blank_slide(prs)
    slide_label(s, "Final verdict — two decisions, clearly separated")
    slide_title(s, "What to choose for a complex multi-microservice OMS")

    # Decision 1
    add_rect(s, Inches(0.5), Inches(1.35), Inches(12.3), Inches(1.55), CARD_BG, SAGA_CLR)
    add_textbox(s, Inches(0.8), Inches(1.42), Inches(11.7), Inches(0.35),
                "Decision 1 — Where does orchestration live?", size=14, bold=True, color=SAGA_CLR)
    add_textbox(s, Inches(0.8), Inches(1.78), Inches(11.7), Inches(1.0),
                "Answer: Java Saga (SagaOrchestratorServiceImpl) — always.\n"
                "Avoid Camel-heavy pipeline for core saga logic. "
                "Our POC proved it is slower (33 ms vs 18 ms POST), harder to test, and compensation in DSL does not scale.",
                size=12, color=WHITE)

    # Decision 2 - two columns
    add_rect(s, Inches(0.5), Inches(3.1), Inches(5.9), Inches(3.5), CARD_BG, SAGA_CLR)
    add_textbox(s, Inches(0.7), Inches(3.18), Inches(5.5), Inches(0.35),
                "Decision 2a — Saga-heavy + Apache Camel", size=13, bold=True, color=SAGA_CLR)
    add_bullets(s, Inches(0.7), Inches(3.55), Inches(5.5), Inches(2.9), [
        "Pick when: org uses Camel, team has Camel skills, future connectors needed",
        "Apache Camel handles protocol bridging only — Kafka in, JSON, bean call. ~20% Camel, ~80% Java",
        "Best latency in our benchmark (18 ms POST, p99 21 ms)",
        "Our default recommendation for enterprise integration teams",
    ], size=11, color=WHITE)

    add_rect(s, Inches(6.9), Inches(3.1), Inches(5.9), Inches(3.5), CARD_BG, SI_CLR)
    add_textbox(s, Inches(7.1), Inches(3.18), Inches(5.5), Inches(0.35),
                "Decision 2b — Saga + Spring Integration", size=13, bold=True, color=SI_CLR)
    add_bullets(s, Inches(7.1), Inches(3.55), Inches(5.5), Inches(2.9), [
        "Pick when: Spring-only team, no Camel footprint, want minimal dependencies",
        "Same saga brain — zero rewrite of business logic",
        "Fully viable: 0% errors, p99 30 ms, same order of magnitude",
        "Our default recommendation for Spring-native product teams",
    ], size=11, color=WHITE)

    # Avoid camel heavy
    add_rect(s, Inches(0.5), Inches(6.75), Inches(3.8), Inches(0.55), CARD_BG, RED,
             "Avoid: Camel-heavy pipeline", 10, RED, True)

    add_rect(s, Inches(4.5), Inches(6.65), Inches(8.3), Inches(0.75), CARD_BG, GREEN)
    add_textbox(s, Inches(4.7), Inches(6.72), Inches(7.9), Inches(0.6),
                "Stakeholder one-liner: Keep saga in Java. Pick Apache Camel or Spring Integration "
                "for Kafka protocol bridging based on your team's skills — not milliseconds. "
                "Never put core OMS orchestration in Camel DSL.",
                size=12, bold=True, color=GREEN, align=PP_ALIGN.CENTER)


def main():
    prs = new_prs()
    slide_01_title(prs)
    slide_02_goal(prs)
    slide_03_baseline_eip(prs)       # NEW — early baseline EIP slide
    slide_04_architecture(prs)
    slide_05_flow(prs)
    slide_06_saga_camel(prs)
    slide_07_camel_heavy(prs)
    slide_08_saga_si(prs)
    slide_09_comparison(prs)
    slide_10_saga_concept(prs)
    slide_11_adapter_concept(prs)
    slide_12_eip_decision_guide(prs)  # NEW — when to choose Camel vs SI
    slide_13_eip_head_to_head(prs)    # NEW — head-to-head saga paths
    slide_14_results(prs)
    slide_15_why_saga(prs)
    slide_16_verdict(prs)             # IMPROVED — two decisions

    out = r"c:\General\knowladge-Base\OMS-POC\benchmark\OMS-POC-Orchestration-Benchmark-v3.pptx"
    prs.save(out)
    print(f"Saved: {out}")
    print(f"Slides: {len(prs.slides)}")


if __name__ == "__main__":
    main()

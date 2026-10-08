"""
Plot metrics from the CloudSim run (metrics.csv) and optional outage annotations.
Run from any directory; resolves CSV paths next to this script.
"""
from pathlib import Path

import matplotlib.pyplot as plt
import pandas as pd

BASE = Path(__file__).resolve().parent
METRICS_CSV = BASE / "metrics.csv"
OUTAGE_CSV = BASE / "outage_history.csv"


def main() -> None:
    if not METRICS_CSV.exists():
        raise SystemExit(
            f"Missing {METRICS_CSV}. Run the Java simulation first to generate metrics.csv."
        )

    df_metrics = pd.read_csv(METRICS_CSV)
    df_metrics.columns = df_metrics.columns.str.strip()

    required = {"Time", "CPU", "RAM", "BW", "EnsembleScore"}
    missing = required - set(df_metrics.columns)
    if missing:
        raise SystemExit(f"metrics.csv is missing columns: {sorted(missing)}. Found: {list(df_metrics.columns)}")

    if df_metrics.empty:
        raise SystemExit(f"{METRICS_CSV} has no data rows. Run the simulation longer or check logging.")

    fig, (ax_util, ax_repair) = plt.subplots(
        2, 1, figsize=(12, 8), sharex=True, gridspec_kw={"height_ratios": [2, 1]}
    )

    ax_util.plot(df_metrics["Time"], df_metrics["CPU"], label="CPU util", color="C0")
    ax_util.plot(df_metrics["Time"], df_metrics["RAM"], label="RAM util", color="C1")
    ax_util.plot(df_metrics["Time"], df_metrics["BW"], label="BW util", color="C2")
    ax_util.set_ylabel("Utilization (0-1)")
    ax_util.set_title("Predictive repair agent — VM metrics, ensemble score, and repair probability")
    ax_util.grid(True, alpha=0.3)

    ax_score = ax_util.twinx()
    ax_score.plot(
        df_metrics["Time"],
        df_metrics["EnsembleScore"],
        label="Ensemble score",
        color="C3",
        linestyle="--",
        linewidth=1.5,
    )
    ax_score.set_ylabel("Ensemble score")

    lines_left, labels_left = ax_util.get_legend_handles_labels()
    lines_right, labels_right = ax_score.get_legend_handles_labels()
    ax_util.legend(lines_left + lines_right, labels_left + labels_right, loc="upper left")

    if OUTAGE_CSV.exists():
        df_outage = pd.read_csv(OUTAGE_CSV)
        df_outage.columns = df_outage.columns.str.strip()
        if not df_outage.empty and "Time" in df_outage.columns and "Layer" in df_outage.columns:
            y_text = float(df_metrics[["CPU", "RAM", "BW"]].max().max()) * 0.95 + 0.02
            # Keep some headroom so we can stagger labels upward.
            y_text = min(y_text, 0.85)
            # Avoid overlapping labels: group by time+layer and then stagger in x/y.
            labels_df = (
                df_outage.groupby(["Time", "Layer"], as_index=False)["RepairProbability"]
                .mean()
                .sort_values(["Time", "Layer"])
            )
            tmin = float(df_metrics["Time"].min())
            tmax = float(df_metrics["Time"].max())
            time_range = max(1e-9, tmax - tmin)
            # Horizontal spread is handled by annotate offsets (in points).
            _ = time_range

            for time_val, group in labels_df.groupby("Time"):
                group = group.reset_index(drop=True)
                n = len(group)
                for j in range(n):
                    row = group.iloc[j]
                    # Offset in points so labels don't collide even with rotation.
                    xoff = (j - (n - 1) / 2.0) * 18
                    yoff = j * 12
                    ax_util.annotate(
                        str(row["Layer"]),
                        xy=(float(time_val), y_text),
                        xytext=(xoff, yoff),
                        textcoords="offset points",
                        rotation=60,
                        fontsize=6,
                        alpha=0.85,
                        ha="center",
                        va="bottom",
                        clip_on=True,
                        bbox=dict(boxstyle="round,pad=0.2", fc="white", alpha=0.45, ec="none"),
                    )
        if (
            not df_outage.empty
            and "Time" in df_outage.columns
            and "RepairProbability" in df_outage.columns
        ):
            repair_by_time = df_outage.groupby("Time", as_index=False)["RepairProbability"].mean()
            ax_repair.fill_between(
                repair_by_time["Time"],
                0,
                repair_by_time["RepairProbability"],
                alpha=0.35,
                color="C4",
                label="Mean repair probability",
            )
            ax_repair.plot(
                repair_by_time["Time"],
                repair_by_time["RepairProbability"],
                color="C4",
                linewidth=1.2,
            )
            ax_repair.set_ylabel("Repair probability (0–1)")
            max_rp = float(repair_by_time["RepairProbability"].max())
            # The probabilities in this simulation can be very small (e.g., 1e-4..1e-3),
            # which makes the curve look flat if we keep y from 0..1.
            if max_rp > 0:
                ax_repair.set_ylim(0, max_rp * 1.2)
                ax_repair.ticklabel_format(axis="y", style="sci", scilimits=(0, 0))
            ax_repair.grid(True, alpha=0.3)
            ax_repair.legend(loc="upper right")
        else:
            ax_repair.text(
                0.5,
                0.5,
                "No outage / repair probability rows",
                ha="center",
                va="center",
                transform=ax_repair.transAxes,
            )
    else:
        ax_repair.text(
            0.5,
            0.5,
            "Missing outage_history.csv",
            ha="center",
            va="center",
            transform=ax_repair.transAxes,
        )

    ax_repair.set_xlabel("Simulation time")

    fig.tight_layout()
    out_png = BASE / "metrics_plot.png"
    fig.savefig(out_png, dpi=150)
    print(f"Saved {out_png}")
    plt.show()


if __name__ == "__main__":
    main()

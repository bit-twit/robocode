/*
 * Decompiled with CFR 0_124.
 * 
 * Could not load the following classes:
 *  java.lang.Double
 *  java.lang.Enum
 *  java.lang.IllegalStateException
 *  java.lang.Integer
 *  java.lang.Object
 *  java.lang.String
 *  java.lang.System
 *  java.util.ArrayList
 *  java.util.Arrays
 *  java.util.LinkedList
 *  java.util.List
 */
package justin.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public abstract class KdTree<T> {
    private static final int bucketSize = 24;
    private final int dimensions;
    private final KdTree<T> parent;
    private final LinkedList<double[]> locationStack;
    private final Integer sizeLimit;
    private double[][] locations;
    private Object[] data;
    private int locationCount;
    private KdTree<T> left;
    private KdTree<T> right;
    private int splitDimension;
    private double splitValue;
    private double[] minLimit;
    private double[] maxLimit;
    private boolean singularity;
    private Status status;

    private KdTree(int dimensions, Integer sizeLimit) {
        this.dimensions = dimensions;
        this.locations = new double[24][];
        this.data = new Object[24];
        this.locationCount = 0;
        this.singularity = true;
        this.parent = null;
        this.sizeLimit = sizeLimit;
        this.locationStack = sizeLimit != null ? new LinkedList() : null;
    }

    private KdTree(KdTree<T> parent, boolean right) {
        this.dimensions = parent.dimensions;
        this.locations = new double[Math.max((int)24, (int)parent.locationCount)][];
        this.data = new Object[Math.max((int)24, (int)parent.locationCount)];
        this.locationCount = 0;
        this.singularity = true;
        this.parent = parent;
        this.locationStack = null;
        this.sizeLimit = null;
    }

    public int size() {
        return this.locationCount;
    }

    public void addPoint(double[] location, T value) {
        KdTree<T> cursor = this;
        while (cursor.locations == null || cursor.locationCount >= cursor.locations.length) {
            if (cursor.locations != null) {
                cursor.splitDimension = cursor.findWidestAxis();
                cursor.splitValue = (cursor.minLimit[cursor.splitDimension] + cursor.maxLimit[cursor.splitDimension]) * 0.5;
                if (cursor.splitValue == Double.POSITIVE_INFINITY) {
                    cursor.splitValue = Double.MAX_VALUE;
                } else if (cursor.splitValue == Double.NEGATIVE_INFINITY) {
                    cursor.splitValue = -1.7976931348623157E308;
                } else if (Double.isNaN((double)cursor.splitValue)) {
                    cursor.splitValue = 0.0;
                }
                if (cursor.minLimit[cursor.splitDimension] == cursor.maxLimit[cursor.splitDimension]) {
                    double[][] newLocations = new double[cursor.locations.length * 2][];
                    System.arraycopy((Object)cursor.locations, (int)0, (Object)newLocations, (int)0, (int)cursor.locationCount);
                    cursor.locations = newLocations;
                    Object[] newData = new Object[newLocations.length];
                    System.arraycopy((Object)cursor.data, (int)0, (Object)newData, (int)0, (int)cursor.locationCount);
                    cursor.data = newData;
                    break;
                }
                if (cursor.splitValue == cursor.maxLimit[cursor.splitDimension]) {
                    cursor.splitValue = cursor.minLimit[cursor.splitDimension];
                }
                ChildNode left = new ChildNode(this, cursor, false, null);
                ChildNode right = new ChildNode(this, cursor, true, null);
                int i = 0;
                while (i < cursor.locationCount) {
                    double[] oldLocation = cursor.locations[i];
                    Object oldData = cursor.data[i];
                    if (oldLocation[cursor.splitDimension] > cursor.splitValue) {
                        right.locations[right.locationCount] = oldLocation;
                        right.data[right.locationCount] = oldData;
                        ++right.locationCount;
                        KdTree.super.extendBounds(oldLocation);
                    } else {
                        left.locations[left.locationCount] = oldLocation;
                        left.data[left.locationCount] = oldData;
                        ++left.locationCount;
                        KdTree.super.extendBounds(oldLocation);
                    }
                    ++i;
                }
                cursor.left = left;
                cursor.right = right;
                cursor.locations = null;
                cursor.data = null;
            }
            ++cursor.locationCount;
            cursor.extendBounds(location);
            cursor = location[cursor.splitDimension] > cursor.splitValue ? cursor.right : cursor.left;
        }
        cursor.locations[cursor.locationCount] = location;
        cursor.data[cursor.locationCount] = value;
        ++cursor.locationCount;
        KdTree.super.extendBounds(location);
        if (this.sizeLimit != null) {
            this.locationStack.add((Object)location);
            if (this.locationCount > this.sizeLimit) {
                KdTree.super.removeOld();
            }
        }
    }

    private final void extendBounds(double[] location) {
        if (this.minLimit == null) {
            this.minLimit = new double[this.dimensions];
            System.arraycopy((Object)location, (int)0, (Object)this.minLimit, (int)0, (int)this.dimensions);
            this.maxLimit = new double[this.dimensions];
            System.arraycopy((Object)location, (int)0, (Object)this.maxLimit, (int)0, (int)this.dimensions);
            return;
        }
        int i = 0;
        while (i < this.dimensions) {
            if (Double.isNaN((double)location[i])) {
                this.minLimit[i] = Double.NaN;
                this.maxLimit[i] = Double.NaN;
                this.singularity = false;
            } else if (this.minLimit[i] > location[i]) {
                this.minLimit[i] = location[i];
                this.singularity = false;
            } else if (this.maxLimit[i] < location[i]) {
                this.maxLimit[i] = location[i];
                this.singularity = false;
            }
            ++i;
        }
    }

    private final int findWidestAxis() {
        int widest = 0;
        double width = (this.maxLimit[0] - this.minLimit[0]) * this.getAxisWeightHint(0);
        if (Double.isNaN((double)width)) {
            width = 0.0;
        }
        int i = 1;
        while (i < this.dimensions) {
            double nwidth = (this.maxLimit[i] - this.minLimit[i]) * this.getAxisWeightHint(i);
            if (Double.isNaN((double)nwidth)) {
                nwidth = 0.0;
            }
            if (nwidth > width) {
                widest = i;
                width = nwidth;
            }
            ++i;
        }
        return widest;
    }

    private void removeOld() {
        double[] location = (double[])this.locationStack.removeFirst();
        KdTree<T> cursor = this;
        while (cursor.locations == null) {
            cursor = location[cursor.splitDimension] > cursor.splitValue ? cursor.right : cursor.left;
        }
        int i = 0;
        while (i < cursor.locationCount) {
            if (cursor.locations[i] == location) {
                System.arraycopy((Object)cursor.locations, (int)(i + 1), (Object)cursor.locations, (int)i, (int)(cursor.locationCount - i - 1));
                cursor.locations[cursor.locationCount - 1] = null;
                System.arraycopy((Object)cursor.data, (int)(i + 1), (Object)cursor.data, (int)i, (int)(cursor.locationCount - i - 1));
                cursor.data[cursor.locationCount - 1] = null;
                do {
                    --cursor.locationCount;
                    cursor = cursor.parent;
                } while (cursor.parent != null);
                return;
            }
            ++i;
        }
    }

    public List<Entry<T>> nearestNeighbor(double[] location, int count, boolean sequentialSorting) {
        ArrayList results;
        KdTree<T> cursor = this;
        cursor.status = Status.NONE;
        double range = Double.POSITIVE_INFINITY;
        ResultHeap resultHeap = new ResultHeap(count);
        do {
            if (cursor.status == Status.ALLVISITED) {
                cursor = cursor.parent;
                continue;
            }
            if (cursor.status == Status.NONE && cursor.locations != null) {
                if (cursor.locationCount > 0) {
                    if (cursor.singularity) {
                        double dist = this.pointDist(cursor.locations[0], location);
                        if (dist <= range) {
                            int i = 0;
                            while (i < cursor.locationCount) {
                                resultHeap.addValue(dist, cursor.data[i]);
                                ++i;
                            }
                        }
                    } else {
                        int i = 0;
                        while (i < cursor.locationCount) {
                            double dist = this.pointDist(cursor.locations[i], location);
                            resultHeap.addValue(dist, cursor.data[i]);
                            ++i;
                        }
                    }
                    range = resultHeap.getMaxDist();
                }
                if (cursor.parent == null) break;
                cursor = cursor.parent;
                continue;
            }
            KdTree<T> nextCursor = null;
            if (cursor.status == Status.NONE) {
                if (location[cursor.splitDimension] > cursor.splitValue) {
                    nextCursor = cursor.right;
                    cursor.status = Status.RIGHTVISITED;
                } else {
                    nextCursor = cursor.left;
                    cursor.status = Status.LEFTVISITED;
                }
            } else if (cursor.status == Status.LEFTVISITED) {
                nextCursor = cursor.right;
                cursor.status = Status.ALLVISITED;
            } else if (cursor.status == Status.RIGHTVISITED) {
                nextCursor = cursor.left;
                cursor.status = Status.ALLVISITED;
            }
            if (cursor.status == Status.ALLVISITED && (nextCursor.locationCount == 0 || !nextCursor.singularity && this.pointRegionDist(location, nextCursor.minLimit, nextCursor.maxLimit) > range)) continue;
            cursor = nextCursor;
            cursor.status = Status.NONE;
        } while (cursor.parent != null || cursor.status != Status.ALLVISITED);
        results = new ArrayList(resultHeap.values);
        if (sequentialSorting) {
            while (resultHeap.values > 0) {
                resultHeap.removeLargest();
                results.add(new Entry(resultHeap.removedDist, resultHeap.removedData, null));
            }
        } else {
            int i = 0;
            while (i < resultHeap.values) {
                results.add(new Entry(resultHeap.distance[i], resultHeap.data[i], null));
                ++i;
            }
        }
        return results;
    }

    protected abstract double pointDist(double[] var1, double[] var2);

    protected abstract double pointRegionDist(double[] var1, double[] var2, double[] var3);

    protected double getAxisWeightHint(int i) {
        return 1.0;
    }

    /* synthetic */ KdTree(KdTree kdTree, boolean bl, KdTree kdTree2) {
        KdTree<T> kdTree3;
        kdTree3(kdTree, bl);
    }

    /* synthetic */ KdTree(int n, Integer n2, KdTree kdTree) {
        KdTree<T> kdTree2;
        kdTree2(n, n2);
    }

    private class ChildNode
    extends KdTree<T> {
        final /* synthetic */ KdTree this$0;

        private ChildNode(KdTree kdTree, KdTree<T> parent, boolean right) {
            this.this$0 = kdTree;
            super(parent, right, null);
        }

        @Override
        protected double pointDist(double[] p1, double[] p2) {
            throw new IllegalStateException();
        }

        @Override
        protected double pointRegionDist(double[] point, double[] min, double[] max) {
            throw new IllegalStateException();
        }

        /* synthetic */ ChildNode(KdTree kdTree, KdTree kdTree2, boolean bl, ChildNode childNode) {
            ChildNode childNode2;
            childNode2(kdTree, kdTree2, bl);
        }
    }

    public static class Entry<T> {
        public final double distance;
        public final T value;

        private Entry(double distance, T value) {
            this.distance = distance;
            this.value = value;
        }

        /* synthetic */ Entry(double d, Object object, Entry entry) {
            Entry<Object> entry2;
            entry2(d, object);
        }
    }

    public static class Manhattan<T>
    extends KdTree<T> {
        public Manhattan(int dimensions, Integer sizeLimit) {
            super(dimensions, sizeLimit, null);
        }

        @Override
        protected double pointDist(double[] p1, double[] p2) {
            double d = 0.0;
            int i = 0;
            while (i < p1.length) {
                double diff = p1[i] - p2[i];
                if (!Double.isNaN((double)diff)) {
                    d += diff < 0.0 ? - diff : diff;
                }
                ++i;
            }
            return d;
        }

        @Override
        protected double pointRegionDist(double[] point, double[] min, double[] max) {
            double d = 0.0;
            int i = 0;
            while (i < point.length) {
                double diff = 0.0;
                if (point[i] > max[i]) {
                    diff = point[i] - max[i];
                } else if (point[i] < min[i]) {
                    diff = min[i] - point[i];
                }
                if (!Double.isNaN((double)diff)) {
                    d += diff;
                }
                ++i;
            }
            return d;
        }
    }

    private static class ResultHeap {
        private final Object[] data;
        private final double[] distance;
        private final int size;
        private int values;
        public Object removedData;
        public double removedDist;

        public ResultHeap(int size) {
            this.data = new Object[size];
            this.distance = new double[size];
            this.size = size;
            this.values = 0;
        }

        public void addValue(double dist, Object value) {
            if (this.values < this.size) {
                this.data[this.values] = value;
                this.distance[this.values] = dist;
                this.upHeapify(this.values);
                ++this.values;
            } else if (dist < this.distance[0]) {
                this.data[0] = value;
                this.distance[0] = dist;
                this.downHeapify(0);
            }
        }

        public void removeLargest() {
            if (this.values == 0) {
                throw new IllegalStateException();
            }
            this.removedData = this.data[0];
            this.removedDist = this.distance[0];
            --this.values;
            this.data[0] = this.data[this.values];
            this.distance[0] = this.distance[this.values];
            this.downHeapify(0);
        }

        private void upHeapify(int c) {
            int p = (c - 1) / 2;
            while (c != 0 && this.distance[c] > this.distance[p]) {
                Object pData = this.data[p];
                double pDist = this.distance[p];
                this.data[p] = this.data[c];
                this.distance[p] = this.distance[c];
                this.data[c] = pData;
                this.distance[c] = pDist;
                c = p;
                p = (c - 1) / 2;
            }
        }

        private void downHeapify(int p) {
            int c = p * 2 + 1;
            while (c < this.values) {
                if (c + 1 < this.values && this.distance[c] < this.distance[c + 1]) {
                    ++c;
                }
                if (this.distance[p] >= this.distance[c]) break;
                Object pData = this.data[p];
                double pDist = this.distance[p];
                this.data[p] = this.data[c];
                this.distance[p] = this.distance[c];
                this.data[c] = pData;
                this.distance[c] = pDist;
                p = c;
                c = p * 2 + 1;
            }
        }

        public double getMaxDist() {
            if (this.values < this.size) {
                return Double.POSITIVE_INFINITY;
            }
            return this.distance[0];
        }
    }

    public static class SqrEuclid<T>
    extends KdTree<T> {
        public SqrEuclid(int dimensions, Integer sizeLimit) {
            super(dimensions, sizeLimit, null);
        }

        @Override
        protected double pointDist(double[] p1, double[] p2) {
            double d = 0.0;
            int i = 0;
            while (i < p1.length) {
                double diff = p1[i] - p2[i];
                if (!Double.isNaN((double)diff)) {
                    d += diff * diff;
                }
                ++i;
            }
            return d;
        }

        @Override
        protected double pointRegionDist(double[] point, double[] min, double[] max) {
            double d = 0.0;
            int i = 0;
            while (i < point.length) {
                double diff = 0.0;
                if (point[i] > max[i]) {
                    diff = point[i] - max[i];
                } else if (point[i] < min[i]) {
                    diff = point[i] - min[i];
                }
                if (!Double.isNaN((double)diff)) {
                    d += diff * diff;
                }
                ++i;
            }
            return d;
        }
    }

    private static enum Status {
        NONE,
        LEFTVISITED,
        RIGHTVISITED,
        ALLVISITED;
        

        private Status(String string2, int n2) {
        }
    }

    public static class WeightedManhattan<T>
    extends KdTree<T> {
        private double[] weights;

        public WeightedManhattan(int dimensions, Integer sizeLimit) {
            super(dimensions, sizeLimit, null);
            this.weights = new double[dimensions];
            Arrays.fill((double[])this.weights, (double)1.0);
        }

        public void setWeights(double[] weights) {
            this.weights = weights;
        }

        @Override
        protected double getAxisWeightHint(int i) {
            return this.weights[i];
        }

        @Override
        protected double pointDist(double[] p1, double[] p2) {
            double d = 0.0;
            int i = 0;
            while (i < p1.length) {
                double diff = p1[i] - p2[i];
                if (!Double.isNaN((double)diff)) {
                    d += (diff < 0.0 ? - diff : diff) * this.weights[i];
                }
                ++i;
            }
            return d;
        }

        @Override
        protected double pointRegionDist(double[] point, double[] min, double[] max) {
            double d = 0.0;
            int i = 0;
            while (i < point.length) {
                double diff = 0.0;
                if (point[i] > max[i]) {
                    diff = point[i] - max[i];
                } else if (point[i] < min[i]) {
                    diff = min[i] - point[i];
                }
                if (!Double.isNaN((double)diff)) {
                    d += diff * this.weights[i];
                }
                ++i;
            }
            return d;
        }
    }

    public static class WeightedSqrEuclid<T>
    extends KdTree<T> {
        private double[] weights;

        public WeightedSqrEuclid(int dimensions, Integer sizeLimit) {
            super(dimensions, sizeLimit, null);
            this.weights = new double[dimensions];
            Arrays.fill((double[])this.weights, (double)1.0);
        }

        public void setWeights(double[] weights) {
            this.weights = weights;
        }

        @Override
        protected double getAxisWeightHint(int i) {
            return this.weights[i];
        }

        @Override
        protected double pointDist(double[] p1, double[] p2) {
            double d = 0.0;
            int i = 0;
            while (i < p1.length) {
                double diff = (p1[i] - p2[i]) * this.weights[i];
                if (!Double.isNaN((double)diff)) {
                    d += diff * diff;
                }
                ++i;
            }
            return d;
        }

        @Override
        protected double pointRegionDist(double[] point, double[] min, double[] max) {
            double d = 0.0;
            int i = 0;
            while (i < point.length) {
                double diff = 0.0;
                if (point[i] > max[i]) {
                    diff = (point[i] - max[i]) * this.weights[i];
                } else if (point[i] < min[i]) {
                    diff = (point[i] - min[i]) * this.weights[i];
                }
                if (!Double.isNaN((double)diff)) {
                    d += diff * diff;
                }
                ++i;
            }
            return d;
        }
    }

}


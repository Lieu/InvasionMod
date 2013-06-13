package mods.invmod.common.entity;


public class NodeContainer
{
    private PathNode pathPoints[];
    private int count;

    public NodeContainer()
    {
        pathPoints = new PathNode[1024];
        count = 0;
    }

    public PathNode addPoint(PathNode pathpoint)
    {
        if (pathpoint.index >= 0)
        {
            throw new IllegalStateException("OW KNOWS!");
        }
        if (count == pathPoints.length)
        {
            PathNode apathpoint[] = new PathNode[count << 1];
            System.arraycopy(pathPoints, 0, apathpoint, 0, count);
            pathPoints = apathpoint;
        }
        pathPoints[count] = pathpoint;
        pathpoint.index = count;
        sortBack(count++);
        return pathpoint;
    }

    public void clearPath()
    {
        count = 0;
    }

    public PathNode dequeue()
    {
        PathNode pathpoint = pathPoints[0];
        pathPoints[0] = pathPoints[--count];
        pathPoints[count] = null;
        if (count > 0)
        {
            sortForward(0);
        }
        pathpoint.index = -1;
        return pathpoint;
    }

    public void changeDistance(PathNode pathpoint, float f)
    {
        float f1 = pathpoint.distanceToTarget;
        pathpoint.distanceToTarget = f;
        if (f < f1)
        {
            sortBack(pathpoint.index);
        }
        else
        {
            sortForward(pathpoint.index);
        }
    }

    private void sortBack(int i)
    {
        PathNode pathpoint = pathPoints[i];
        float f = pathpoint.distanceToTarget;
        do
        {
            if (i <= 0)
            {
                break;
            }
            int j = i - 1 >> 1;
            PathNode pathpoint1 = pathPoints[j];
            if (f >= pathpoint1.distanceToTarget)
            {
                break;
            }
            pathPoints[i] = pathpoint1;
            pathpoint1.index = i;
            i = j;
        }
        while (true);
        pathPoints[i] = pathpoint;
        pathpoint.index = i;
    }

    private void sortForward(int i)
    {
        PathNode pathpoint = pathPoints[i];
        float f = pathpoint.distanceToTarget;
        do
        {
            int j = 1 + (i << 1);
            int k = j + 1;
            if (j >= count)
            {
                break;
            }
            PathNode pathpoint1 = pathPoints[j];
            float f1 = pathpoint1.distanceToTarget;
            PathNode pathpoint2;
            float f2;
            if (k >= count)
            {
                pathpoint2 = null;
                f2 = (1.0F / 0.0F);
            }
            else
            {
                pathpoint2 = pathPoints[k];
                f2 = pathpoint2.distanceToTarget;
            }
            if (f1 < f2)
            {
                if (f1 >= f)
                {
                    break;
                }
                pathPoints[i] = pathpoint1;
                pathpoint1.index = i;
                i = j;
                continue;
            }
            if (f2 >= f)
            {
                break;
            }
            pathPoints[i] = pathpoint2;
            pathpoint2.index = i;
            i = k;
        }
        while (true);
        pathPoints[i] = pathpoint;
        pathpoint.index = i;
    }

    public boolean isPathEmpty()
    {
        return count == 0;
    }
}

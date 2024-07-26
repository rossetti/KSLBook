The KSL has default scoring models. Two of the scoring models are based on a histogram summary of the data. This involves dividing the range of observations via break points $b_j$ and tabulating frequencies or proportions of the data falling with the defined intervals. 

The KSL does not use arbitrary break points from a histogram generation process. Instead the KSL attempts to define the break points for the ntervals such that each interval has the *same* probability of occurrence.  This also ensures that the expected number of observations within each interval is approximately the same. The theoretical basis for this approach can be found in [@CAWilliams1950].  [@CAWilliams1950] considered the testing of $U(0,1)$ random variates.  In the case of the $U(0,1)$ distribution, the choice of the number of intervals determines the break points because each interval is equally likely. [@CAWilliams1950] recommended choosing the class limits such that the expected number in the interval was $n/k$, where $n$ is the number of observations and $k$ is the number of class intervals. Based on the ability to ensure that the resulting chi-squared test statistic actually has a chi-squared distribution, [@CAWilliams1950] recommended that the number of class intervals be:

\begin{equation}
k = \Bigg\lceil 4 \, \sqrt[5]{\frac{2(n-1)^2}{z_{1-\alpha}}} \, \Bigg\rceil
\end{equation} 

This approach produces equally distant break points between $(0,1)$.  Let's call those break points $u_i$ for $i=1,\cdots,k$.  We then define the break points for the chi-squared test for the distribution, $F(x)$, as $b_i = F^{-1}(u_i)$, which will result in non-equally spaced break points for $F(x)$, but the probability $p_i$ associated with the intervals will all be the same.  For the resulting recommended break points, the procedure attempts to ensure that the number of intervals is at least 3 and that the expected number within the intervals is at least 5. If the number of observations of the sample, $n$, is less than or equal to 15, this may not be possible. The procedure ensures that there are at least 3 intervals and if the expected count is less than 5 for any interval the user is warned. This same procedure is used in determining the break points for the squared error criteria.  This approach reduces the sensitivity of the chi-squared fitting process and the squared error criteria to the choice of the intervals.


Let $c_{j}$ be the observed count of the $x$ values contained in
the $j^{th}$ interval $\left(b_{j-1}, b_{j} \right]$, $h_j = c_j/n$ be the relative frequency for the $j^{th}$ interval, and 

$$p_j = P\{b_{j-1} \leq X \leq b_{j}\} = \int\limits_{b_{j-1}}^{b_{j}} f(x) \mathrm{d}x$$

be the theoretical probability associated with the interval. The squared error criterion and the chi-squared criterion are based on the defined intervals.

- Squared error criterion - The squared error is defined as the sum over the intervals of the squared difference between the relative frequency and the theoretical probability associated with each interval:

\begin{equation}
\text{Square Error} = \sum_{j = 1}^k (h_j - p_j)^2
\end{equation}

- Chi-Squared criterion - The chi-squared criterion is the $\chi^{2}$ test statistic value that compares the observed counts $c_j$ to the expected counts $np_j$ over the intervals. 

\begin{equation}
\chi^{2}_{0} = \sum\limits_{j=1}^{k} \frac{\left( c_{j} - np_{j} \right)^{2}}{np_{j}}
\end{equation}

- Kolmogorov-Smirnov criterion - The Kolmogorov-Smirnov criterion, $D_{n} = \max \lbrace D^{+}_{n}, D^{-}_{n} \rbrace$, represents the largest vertical
distance between the hypothesized distribution and the empirical
distribution over the range of the distribution.

$$
\begin{aligned}
D^{+}_{n} & = \underset{1 \leq i \leq n}{\max} \Bigl\lbrace \tilde{F}_{n} \left( x_{(i)} \right) -  \hat{F}(x_{(i)}) \Bigr\rbrace \\
  & = \underset{1 \leq i \leq n}{\max} \Bigl\lbrace \frac{i}{n} -  \hat{F}(x_{(i)}) \Bigr\rbrace \end{aligned}
$$
$$
\begin{aligned}
D^{-}_{n} & = \underset{1 \leq i \leq n}{\max} \Bigl\lbrace \hat{F}(x_{(i)}) - \tilde{F}_{n} \left( x_{(i-1)} \right) \Bigr\rbrace \\
  & = \underset{1 \leq i \leq n}{\max} \Bigl\lbrace \hat{F}(x_{(i)}) - \frac{i-1}{n} \Bigr\rbrace
\end{aligned}
$$
- Cramer Von Mises criterion - The Cramer Von Mises criterion is a distance measure used to compare the goodness of fit of a cumulative distribution function, $F(x)$ to the empirical distribution, $F_n(x)$.  The distance is defined as:

\begin{equation}
\omega^2 = \int_{-\infty}^{+\infty}\Big[ F_n(x) - F(x) \Big]^2\,dF(x)
\end{equation}

This metric can be computed from data sorted in ascending order ($x_1, x_2, \cdots, x_n$), i.e. the order statistics, as:

\begin{equation}
T = n\omega^2 = \frac{1}{12n} + \sum_{i=1}^{n}\Big[ \frac{2i-1}{2n} - F(x_i) \Big]^2
\end{equation} 

- Anderson Darling criterion - The Anderson-Darling criterion is similar in spirit to the Cramer Von Mises test statistic except that it is designed to detect discrepancies in the tails of the distribution. 

\begin{equation}
A^2 = n\int_{-\infty}^{+\infty}\frac{\Big[ F_n(x) - F(x) \Big]^2}{F(x)(1-F(x))}\,dF(x)
\end{equation}

This metric can be computed from data sorted in ascending order ($x_1, x_2, \cdots, x_n$), i.e. the order statistics, as:
\begin{equation}
A^2 = -n - \sum_{i=1}^{n}\frac{2i-1}{n}\Big[ \ln(F(x_i)) + \ln(1-F(x_{n+1-i})) \Big]
\end{equation} 

- P-P Plot Sum of Squared Errors - Based on the concepts found in [@gan-koehler], the sum of squared error related to the P-P plot of the theoretical distribution versus the empirical distribution was developed as a scoring criterion. Let $(x_{(1)}, x_{(2)}, \ldots x_{(n)})$ be the order statistics. Let the theoretical distribution be represented with $\hat{F}(x_{(i)})$ for i= 1, 2, $\ldots$ n where $\hat{F}$ is the CDF of the hypothesized distribution. Define the empirical distribution as

$$\tilde{F}_n(x_{(i)}) = \dfrac{i - 0.5}{n}$$
Then, the P-P Plot sum of squared error criterion is defined as:

$$
\text{PP Squared Error} = \sum_{i = 1}^n (\tilde{F}_n(x_{(i)}) - \hat{F}(x_{(i)}))^2
$$

- P-P Correlation - Based on the concepts found in [@gan-koehler], the P-P correlation scoring model computes the Pearson correlation associated with a P-P plot.  That is, the scoring model computes the correlation between $(\tilde{F}_n(x_{(i)}), \hat{F}(x_{(i)}))$.

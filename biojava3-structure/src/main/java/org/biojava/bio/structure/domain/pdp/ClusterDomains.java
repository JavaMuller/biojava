package org.biojava.bio.structure.domain.pdp;

import java.util.ArrayList;
import java.util.List;

import org.biojava.bio.structure.domain.ProteinDomainParser;

public class ClusterDomains {


	static private boolean verbose = CutDomain.verbose;

	private static int ndom;
	public static List<Domain> cluster(List<Domain> domains, PDPDistanceMatrix pdpDistMatrix){

		ndom = domains.size();

		
		int Si = -1;
		int Sj = -1;
		int Sis = -1;
		int Sjs = -1;
		int Sim = -1;
		int Sjm = -1;

		long total_max_contacts = 0;

		double maximum_values = PDPParameters.CUT_OFF_VALUE1S;
		double maximum_valuem = PDPParameters.CUT_OFF_VALUE1M;
		double maximum_value  = PDPParameters.CUT_OFF_VALUE1;
		


		if (ndom < 2) return domains;

		/*
	for(i=0;i<ndom;i++)
		domains.get(i).avd=domcont(domains.get(i));
		 */

		if(verbose) ProteinDomainParser.listdomains (domains);

		do {
			for(int i=0;i<ndom-1;i++) {
				for(int j=i+1;j<ndom;j++) {
					Domain d1 = domains.get(i);
					Domain d2 = domains.get(j);
					long total_contacts = getTotalContacts(domains,pdpDistMatrix,d1,d2);
					System.out.println(" pos: d1:" + i + " vs d2:" +j + " d1:" + d1.getSegment(0).from + "-" + d1.getSegment(0).to + " " +  d2.getSegment(0).from + "-" + d2.getSegment(0).to + " " + total_contacts);
					int size1dom1=domains.get(i).size;
					int size2dom2=domains.get(j).size;
					double minDomSize=Math.min(size1dom1,size2dom2);
					double maxDomSize=Math.max(size1dom1,size2dom2);
					
					
					// set some limits on how big the domains can get
					if(minDomSize>150&&maxDomSize>1.5*minDomSize) maxDomSize=1.5*minDomSize;
					else if(maxDomSize>2*minDomSize) maxDomSize=2*minDomSize;
					
					long size1= new Double(Math.min((double)PDPParameters.MAXSIZE,minDomSize)).longValue();
					long size2= new Double(Math.min((double)PDPParameters.MAXSIZE,maxDomSize)).longValue();
					minDomSize=Math.min(Math.pow(minDomSize,1.6/3)+PDPParameters.RG1,Math.pow(minDomSize,1.4/3)+Math.pow(PDPParameters.TD1,1.6/3)+PDPParameters.RG1);
					maxDomSize=Math.min(Math.pow(maxDomSize,1.6/3)+PDPParameters.RG1,Math.pow(maxDomSize,1.4/3)+Math.pow(PDPParameters.TD1,1.6/3)+PDPParameters.RG1);

					/*
				total_max_contacts = 10.+
	(long)( (10.*(size1)/(size1+10.)+(size1)*pow((double)(size1),(double)(2./3.))/(size1+10.))*(10.*(size2)/(size2+10.)+(size2)*pow((double)(size2),(double)(2./3.))/(size2+10.)));
				total_max_contacts = (min(200,size1))*(min(200,size2))/4;
					 */
					/*
				total_max_contacts = min(x*y,MAXCONT);
					 */
					total_max_contacts=new Double(minDomSize*maxDomSize*10).longValue();
					if(size1>130) total_max_contacts=new Double(minDomSize*maxDomSize*9).longValue();
					/*
	avd=(domains.get(i).avd+domains.get(j).avd)/2;

				S_value=(double)total_contacts/total_max_contacts/avd;
					 */
					
					double S_value= total_contacts/(double)total_max_contacts;
					if(verbose) System.out.println(String.format(" size1=%d size2=%d minDomSize=%5.2f maxDomSize=%5.2f total_contacts = %d ", size1,size2,minDomSize,maxDomSize,total_contacts));
					if(verbose) System.out.println(String.format(" total_contacts = %d total_max_contacts = %d", total_contacts, total_max_contacts));
					if(verbose) System.out.println(String.format(" maximum_value = %f S_value = %f\n",maximum_value, S_value));
					
					if (S_value > maximum_value) {
						maximum_value = S_value;
						Si = i;
						Sj = j;
					}
					if (S_value > maximum_valuem&&size1<70) {
						maximum_valuem = S_value;
						Sim = i;
						Sjm = j;
					}
					if (S_value > maximum_values&&size1<52) {
						maximum_values = S_value;
						Sis = i;
						Sjs = j;
					}
					total_contacts = 0;
					total_max_contacts = 0;
				}
			}

			if ( verbose) {
				System.out.println("Check for combining: " + maximum_value  + " 1 :" + PDPParameters.CUT_OFF_VALUE1);
				System.out.println("                     " + maximum_valuem + " 1M:" + PDPParameters.CUT_OFF_VALUE1M );
				System.out.println("                     " + maximum_values + " 1S:" + PDPParameters.CUT_OFF_VALUE1S);
			}
			
			if (maximum_value > PDPParameters.CUT_OFF_VALUE1) {
				/*
			avd=(domains.get(Si).avd+domains.get(Sj).avd)/2;
				 */
				if(verbose) System.out.println(" Criteria 1 matched");
				if(verbose) System.out.println(String.format(" maximum_value = %f", maximum_value));
				if(verbose) System.out.println(String.format(" Si = %d Sj = %d ", Si, Sj));
				domains = combine(domains,Si, Sj, maximum_value);
				maximum_value = PDPParameters.CUT_OFF_VALUE1-.1;
				maximum_values = PDPParameters.CUT_OFF_VALUE1S-.1;
				maximum_valuem = PDPParameters.CUT_OFF_VALUE1M-.1;
				/*
			domains.get(Si).avd=domcont(domains.get(Si));
			domains.get(Sj).avd=domcont(domains.get(Sj));
				 */
				if(verbose) System.out.println(String.format(" Listing the domains after combining..."));
				if(verbose) ProteinDomainParser.listdomains (domains);
			}
			else if (maximum_valuem > PDPParameters.CUT_OFF_VALUE1M) {
				/*
			avd=(domains[Sim].avd+domains[Sjm].avd)/2;
				 */
				if(verbose) System.out.println(" Criteria 2 matched");
				if(verbose) System.out.println(String.format(" maximum_values = %f", maximum_valuem));
				if(verbose) System.out.println(String.format(" Sim = %d Sjm = %d", Sim, Sjm));
				domains = combine(domains, Sim, Sjm, maximum_valuem);
				maximum_value =  PDPParameters.CUT_OFF_VALUE1-.1;
				maximum_values = PDPParameters.CUT_OFF_VALUE1S-.1;
				maximum_valuem = PDPParameters.CUT_OFF_VALUE1M-.1;
				/*
			domains[Sim].avd=domcont(domains[Sim]);
			domains[Sjm].avd=domcont(domains[Sjm]);
				 */
				if(verbose) System.out.println(String.format(" Listing the domains after combining..."));
				if(verbose) ProteinDomainParser.listdomains (domains);
			}
			else if (maximum_values > PDPParameters.CUT_OFF_VALUE1S) {
				/*
			avd=(domains[Sis].avd+domains[Sjs].avd)/2;
				 */
				if(verbose) System.out.println(" Criteria 3 matched");
				if(verbose) System.out.println(String.format(" maximum_values = %f", maximum_values));
				if(verbose) System.out.println(String.format(" Sis = %d Sjs = %d", Sis, Sjs));
				domains = combine(domains, Sis, Sjs, maximum_values);
				maximum_value = PDPParameters.CUT_OFF_VALUE1-.1;
				maximum_values = PDPParameters.CUT_OFF_VALUE1S-.1;
				maximum_valuem = PDPParameters.CUT_OFF_VALUE1M-.1;
				/*
			domains[Sis].avd=domcont(domains[Sis]);
			domains[Sjs].avd=domcont(domains[Sjs]);
				 */
				if(verbose) System.out.println(String.format(" Listing the domains after combining..."));
				if(verbose) ProteinDomainParser.listdomains(domains);
			}
			else {
				if(verbose) System.out.println(String.format(" Maximum value is less than cut off value. (max:" + maximum_value+")" ));
				maximum_value = -1.0;
				maximum_values = -1.0;
				maximum_valuem = -1.0;

			}
		} while ( maximum_value > 0.0||maximum_values>0.0||maximum_valuem>0.0);

		if(verbose) System.out.println(String.format(" The domains are:"));
		if(verbose) ProteinDomainParser.listdomains(domains);
		return domains;
	}



	private static long getTotalContacts(List<Domain> domains,
			PDPDistanceMatrix pdpDistMatrix, Domain i, Domain j) {
		long total_contacts=0;
		
		
		
		
		for(int k=0;k<i.nseg;k++) {
			for(int l=0;l<j.nseg;l++) {
				long contacts = calc_S(j.getSegment(l).from, 
						j.getSegment(l).to,
						i.getSegment(k).from,
						i.getSegment(k).to,
						pdpDistMatrix);
				total_contacts += contacts;
			}
		}
		return total_contacts;
	}



	private static List<Domain> combine(List<Domain> domains,int Si, int Sj, double maximum_value) {

		if ( verbose)
			System.out.println("  +++  combining domains " + Si + " " + Sj);
		
		List<Domain> newdoms = new ArrayList<Domain>();

		//int ndom = domains.size();
		for(int i=0;i<domains.get(Sj).nseg;i++) {
			domains.get(Si).getSegment(domains.get(Si).nseg).from=domains.get(Sj).getSegment(i).from;
			domains.get(Si).getSegment(domains.get(Si).nseg).to=domains.get(Sj).getSegment(i).to;
			domains.get(Si).nseg++;
		}
		domains.get(Si).size+=domains.get(Sj).size;


		for(int i=0;i<domains.get(ndom-1).nseg;i++) {
			domains.get(Sj).getSegment(i).from=domains.get(ndom-1).getSegment(i).from;
			domains.get(Sj).getSegment(i).to=domains.get(ndom-1).getSegment(i).to;

		}
		for ( int i =0; i < domains.size(); i++){
			if ( i == Sj)continue;
			newdoms.add(domains.get(i));
		}
		domains.get(Sj).size=domains.get(ndom-1).size;
		domains.get(Sj).nseg=domains.get(ndom-1).nseg;

		ndom--;
		return newdoms;

	} 

	private static long calc_S (int a1,int b1,int a2,int b2, PDPDistanceMatrix pdpDistMatrix)
	{
		
		long contacts = 0;

		int[][] dist = pdpDistMatrix.getDist();

		for(int i=a1;i<=b1;i++) 
			for(int j=a2;j<=b2;j++) 
				contacts+=dist[i][j];

		return contacts;
	}
}

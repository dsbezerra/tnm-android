package com.tnmlicitacoes.app.mupdf;

public interface CancellableTaskDefinition <Params, Result>
{
	Result doInBackground(Params... params);
	void doCancel();
	void doCleanup();
}
